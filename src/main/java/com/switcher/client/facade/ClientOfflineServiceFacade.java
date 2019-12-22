package com.switcher.client.facade;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;

import com.switcher.client.domain.CriteriaResponse;
import com.switcher.client.domain.Entry;
import com.switcher.client.domain.Switcher;
import com.switcher.client.domain.criteria.Config;
import com.switcher.client.domain.criteria.Domain;
import com.switcher.client.domain.criteria.Group;
import com.switcher.client.domain.criteria.Strategy;
import com.switcher.client.exception.SwitcherKeyNotFoundException;
import com.switcher.client.utils.SwitcherUtils;

public class ClientOfflineServiceFacade {
	
	final static Logger logger = Logger.getLogger(ClientOfflineServiceFacade.class);
	
	public static final String DATE_REGEX = "([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))";
	public static final String CIDR_REGEX = "^([0-9]{1,3}\\.){3}[0-9]{1,3}(\\/([0-9]|[1-2][0-9]|3[0-2]))";
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String DISABLED_DOMAIN = "Domain disabled";
	public static final String DISABLED_GROUP = "Group disabled";
	public static final String DISABLED_CONFIG = "Config disabled";

	private static ClientOfflineServiceFacade instance;

	private ClientOfflineServiceFacade() {}

	public static ClientOfflineServiceFacade getInstance() {
		
		if (instance == null) {
			instance = new ClientOfflineServiceFacade();
		}
		return instance;
	}

	public CriteriaResponse executeCriteria(final Switcher switcher, final Domain domain) throws Exception {

		if (!domain.isActivated()) {
			return new CriteriaResponse(false, DISABLED_DOMAIN);
		}

		Config configFound = null;
		for (final Group group : domain.getGroup()) {
			configFound = Arrays.stream(group.getConfig())
					.filter(config -> config.getKey().equals(switcher.getKey()))
					.findFirst()
					.orElse(null);

			if (configFound != null) {
				if (!group.isActivated()) {
					return new CriteriaResponse(false, DISABLED_GROUP);
				}

				if (!configFound.isActivated()) {
					return new CriteriaResponse(false, DISABLED_CONFIG);
				}

				if (ArrayUtils.isNotEmpty(configFound.getStrategies())) {
					try {
						this.processOperation(configFound.getStrategies(), switcher.getEntry());
					} catch (Exception e) {
						logger.error(e);
						return new CriteriaResponse(false, e.getMessage());
					}
				}
				break;
			}
		}

		if (configFound == null) {
			throw new SwitcherKeyNotFoundException(switcher.getKey());
		}

		return new CriteriaResponse(true, "Success");
	}

	private boolean processOperation(final Strategy[] configStrategies, final List<Entry> input) throws Exception {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("configStrategies: %s", Arrays.toString(configStrategies)));
			logger.debug(String.format("input: %s", Arrays.toString(input != null ? input.toArray() : ArrayUtils.EMPTY_STRING_ARRAY)));
		}
		
		for (final Strategy strategy : configStrategies) {
			
			if (!strategy.isActivated()) {
				continue;
			}

			final Entry switcherInput = input != null ? 
					input.stream().filter(i -> i.getStrategy().equals(strategy.getStrategy())).findFirst().orElse(null) : null;

			if (switcherInput == null) {
				throw new Exception(String.format("Strategy %s did not receive any input", strategy.getStrategy()));
			}

			boolean result = true;
			switch (strategy.getStrategy()) {
			case Entry.VALUE:
				result = this.processValue(strategy, switcherInput);
				break;
			case Entry.NETWORK:
				result = this.processNetwork(strategy, switcherInput);
				break;
			case Entry.DATE:
				result = this.processDate(strategy, switcherInput);
				break;
			case Entry.TIME:
				result = this.processTime(strategy, switcherInput);
				break;
			default:
				result = false;	
			}

			if (!result) {
				throw new Exception(String.format("`Strategy %s does not agree", strategy.getStrategy()));
			}
		}

		return false;
	}

	private boolean processNetwork(final Strategy strategy, final Entry switcherInput) {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("strategy: %s", strategy));
			logger.debug(String.format("switcherInput: %s", switcherInput));
		}

		SubnetUtils subUtils;
		switch (strategy.getOperation()) {
		case Entry.EXIST:
			for (final String value : strategy.getValues()) {
				if (value.matches(CIDR_REGEX)) {
					subUtils = new SubnetUtils(value);

					if (subUtils.getInfo().isInRange(switcherInput.getInput())) {
						return true;
					}
				} else {
					if (value.equals(switcherInput.getInput())) {
						return true;
					}
				}
			}
			break;
		case Entry.NOT_EXIST:
			strategy.setOperation(Entry.EXIST);
			return !processNetwork(strategy, switcherInput);
		default:
			break;
		}
		return false;
	}

	private boolean processValue(final Strategy strategy, final Entry switcherInput) {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("strategy: %s", strategy));
			logger.debug(String.format("switcherInput: %s", switcherInput));
		}
		
		switch (strategy.getOperation()) {
		case Entry.EXIST:
			return Arrays.stream(strategy.getValues())
					.filter(val -> val.equals(switcherInput.getInput()))
					.findFirst()
					.isPresent();
		case Entry.NOT_EXIST:
			strategy.setOperation(Entry.EXIST);
			return !processValue(strategy, switcherInput);
		case Entry.EQUAL:
			return strategy.getValues().length == 1 && strategy.getValues()[0].equals(switcherInput.getInput());
		case Entry.NOT_EQUAL:
			return strategy.getValues().length == 1 && !strategy.getValues()[0].equals(switcherInput.getInput());
		default:
			break;
		}
		return false;
	}

	private boolean processDate(final Strategy strategy, final Entry switcherInput) throws Exception {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("strategy: %s", strategy));
			logger.debug(String.format("switcherInput: %s", switcherInput));
		}
		
		try {
			Date stgDate, stgDate2, inputDate;

			switch (strategy.getOperation()) {
			case Entry.LOWER:
				stgDate = DateUtils.parseDate(SwitcherUtils.getFullDate(strategy.getValues()[0]), DATE_FORMAT);
				inputDate = DateUtils.parseDate(SwitcherUtils.getFullDate(switcherInput.getInput()), DATE_FORMAT);

				return inputDate.before(stgDate);
			case Entry.GREATER:
				stgDate = DateUtils.parseDate(SwitcherUtils.getFullDate(strategy.getValues()[0]), DATE_FORMAT);
				inputDate = DateUtils.parseDate(SwitcherUtils.getFullDate(switcherInput.getInput()), DATE_FORMAT);

				return inputDate.after(stgDate);
			case Entry.BETWEEN:
				if (strategy.getValues().length == 2) {
					stgDate = DateUtils.parseDate(SwitcherUtils.getFullDate(strategy.getValues()[0]), DATE_FORMAT);
					stgDate2 = DateUtils.parseDate(SwitcherUtils.getFullDate(strategy.getValues()[1]), DATE_FORMAT);
					inputDate = DateUtils.parseDate(SwitcherUtils.getFullDate(switcherInput.getInput()), DATE_FORMAT);

					return inputDate.after(stgDate) && inputDate.before(stgDate2);
				}
			default:
				break;
			}

		} catch (ParseException e) {
			logger.error(e);
			throw new Exception("Something went wrong while trying to process the date validation", e);
		}

		return false;
	}

	private boolean processTime(final Strategy strategy, final Entry switcherInput) throws Exception {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("strategy: %s", strategy));
			logger.debug(String.format("switcherInput: %s", switcherInput));
		}
		
		try {
			final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			final String today = format.format(new Date());

			Date stgDate, stgDate2, inputDate;

			switch (strategy.getOperation()) {
			case Entry.LOWER:
				stgDate = DateUtils.parseDate(SwitcherUtils.getFullTime(today, strategy.getValues()[0]), DATE_FORMAT);
				inputDate = DateUtils.parseDate(SwitcherUtils.getFullTime(today, switcherInput.getInput()), DATE_FORMAT);

				return inputDate.before(stgDate);
			case Entry.GREATER:
				stgDate = DateUtils.parseDate(SwitcherUtils.getFullTime(today, strategy.getValues()[0]), DATE_FORMAT);
				inputDate = DateUtils.parseDate(SwitcherUtils.getFullTime(today, switcherInput.getInput()), DATE_FORMAT);

				return inputDate.after(stgDate);
			case Entry.BETWEEN:
				if (strategy.getValues().length == 2) {
					stgDate = DateUtils.parseDate(SwitcherUtils.getFullTime(today, strategy.getValues()[0]), DATE_FORMAT);
					stgDate2 = DateUtils.parseDate(SwitcherUtils.getFullTime(today, strategy.getValues()[1]), DATE_FORMAT);
					inputDate = DateUtils.parseDate(SwitcherUtils.getFullTime(today, switcherInput.getInput()), DATE_FORMAT);

					return inputDate.after(stgDate) && inputDate.before(stgDate2);
				}
			default:
				break;
			}

		} catch (ParseException e) {
			logger.error(e);
			throw new Exception("Something went wrong while trying to process the time validation", e);
		}

		return false;
	}

}
