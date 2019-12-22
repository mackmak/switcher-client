package com.switcher.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.switcher.client.domain.AuthRequest;
import com.switcher.client.domain.Entry;
import com.switcher.client.domain.Switcher;
import com.switcher.client.exception.SwitcherKeyNotFoundException;
import com.switcher.client.utils.SwitcherContextParam;

@RunWith(PowerMockRunner.class)
public class SwitcherOfflineTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources/";
	
	private Map<String, Object> properties;
	
	@Before
	public void setupContext() {

		properties = new HashMap<String, Object>();
		properties.put(SwitcherContextParam.URL, "http://localhost:3000/criteria");
		properties.put(SwitcherContextParam.APIKEY, "$2b$08$S2Wj/wG/Rfs3ij0xFbtgveDtyUAjML1/TOOhocDg5dhOaU73CEXfK");
		properties.put(SwitcherContextParam.DOMAIN, "switcher-domain");
		properties.put(SwitcherContextParam.COMPONENT, "switcher-client");
		properties.put(SwitcherContextParam.ENVIRONMENT, "default");
	}
	
	@Test
	public void offlineShouldReturnTrue() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE11");
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE12");
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_groupDisabled() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE21");
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_domainDisabled() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture2.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE111");
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_strategyDisabled() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		// There is a disabled strategy requiring value validation.
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE71");
		assertTrue(switcher.isItOn());
	}
	
	@Test(expected = SwitcherKeyNotFoundException.class)
	public void offlineShouldNotReturn_keyNotFound() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("NOT_FOUND_KEY");
		switcher.isItOn();
	}
	
	@Test
	public void offlineShouldReturnTrue_dateValidationGreater() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE31");
		Entry input = new Entry(Entry.DATE, "2019-12-11");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_dateValidationGreater() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE31");
		Entry input = new Entry(Entry.DATE, "2019-12-09");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_dateValidationLower() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE32");
		Entry input = new Entry(Entry.DATE, "2019-12-09");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_dateValidationLower() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE32");
		Entry input = new Entry(Entry.DATE, "2019-12-12");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_dateValidationBetween() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE33");
		Entry input = new Entry(Entry.DATE, "2019-12-11");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_dateValidationBetween() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE33");
		Entry input = new Entry(Entry.DATE, "2019-12-13");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_dateValidationWrongFormat() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE33");
		Entry input = new Entry(Entry.DATE, "2019/121/13");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_valueValidationExist() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE41");
		Entry input = new Entry(Entry.VALUE, "Value1");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_valueValidationExist() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE41");
		Entry input = new Entry(Entry.VALUE, "Value5");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_valueValidationNotExist() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE42");
		Entry input = new Entry(Entry.VALUE, "Value5");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_valueValidationNotExist() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE42");
		Entry input = new Entry(Entry.VALUE, "Value1");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_valueValidationEqual() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE43");
		Entry input = new Entry(Entry.VALUE, "Value1");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_valueValidationEqual() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE43");
		Entry input = new Entry(Entry.VALUE, "Value2");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_valueValidationNotEqual() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE44");
		Entry input = new Entry(Entry.VALUE, "Value2");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_valueValidationNotEqual() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE44");
		Entry input = new Entry(Entry.VALUE, "Value1");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_timeValidationGreater() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE51");
		Entry input = new Entry(Entry.TIME, "11:00");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_timeValidationGreater() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE51");
		Entry input = new Entry(Entry.TIME, "09:00");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_timeValidationLower() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE52");
		Entry input = new Entry(Entry.TIME, "09:00");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_timeValidationLower() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE52");
		Entry input = new Entry(Entry.TIME, "11:00");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_timeValidationBetween() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE53");
		Entry input = new Entry(Entry.TIME, "13:00");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_timeValidationBetween() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE53");
		Entry input = new Entry(Entry.TIME, "18:00");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_timeValidationWrongFormat() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE53");
		Entry input = new Entry(Entry.TIME, "2019-12-10");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_networkValidationExistCIDR() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE61");
		Entry input = new Entry(Entry.NETWORK, "10.0.0.4");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_networkValidationExistCIDR() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE61");
		Entry input = new Entry(Entry.NETWORK, "10.0.0.8");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_networkValidationNotExistCIDR() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE62");
		Entry input = new Entry(Entry.NETWORK, "10.0.0.8");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_networkValidationNotExistCIDR() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE62");
		Entry input = new Entry(Entry.NETWORK, "10.0.0.5");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_networkValidationExist() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE63");
		Entry input = new Entry(Entry.NETWORK, "10.0.0.2");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_networkValidationExist() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE63");
		Entry input = new Entry(Entry.NETWORK, "10.0.0.5");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_strategyRequiresInput() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE63");
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void shouldCreateAuthRequest() throws Exception {
		final AuthRequest authRequest = new AuthRequest();
		authRequest.setDomain((String) properties.get(SwitcherContextParam.DOMAIN));
		authRequest.setComponent((String) properties.get(SwitcherContextParam.COMPONENT));
		authRequest.setEnvironment((String) properties.get(SwitcherContextParam.ENVIRONMENT));
		
		assertNotNull(authRequest.toString());
	}
	


}
