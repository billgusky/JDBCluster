package test.privilege;

import java.util.Set;

import junit.framework.TestCase;
import mycluster.CBicycle;
import mycluster.NestedPropertyForPrivTest;
import mycluster.privilege.UserPrivilege;

import org.apache.log4j.PropertyConfigurator;
import org.jdbcluster.JDBClusterSimpleConfig;
import org.jdbcluster.clustertype.ClusterType;
import org.jdbcluster.clustertype.ClusterTypeFactory;
import org.jdbcluster.metapersistence.cluster.ClusterFactory;

public class TestPrivilege extends TestCase {

	@Override
	protected void setUp() throws Exception {
		// configuring logging
		PropertyConfigurator.configure("xml/logging.properties");

		JDBClusterSimpleConfig.setConfiguration("test/test/domain/jdbcluster.conf.TestDomainCheck.xml");
		//JDBClusterSimpleConfig.setHibernateConfiguration(null, "mapping/hibernate.cfg.xml");
		super.setUp();
	}

	public void testPrivilege() {
		// we use cluster auto now
		ClusterType cFahrradType = ClusterTypeFactory.newInstance("bicycle");

		// create a Cluster and persist it
		CBicycle rad = ClusterFactory.newInstance(cFahrradType, null);
		
		Set<String> lastPrivSet;

		
		/**
		 * needed rights for setColorType
		 * this is a domain without dependancies
		 */
		
		rad.setColorType("Color");
		
		lastPrivSet = UserPrivilege.getPrivSet();
		assertTrue(lastPrivSet.size()==3);
		assertTrue(lastPrivSet.contains("BIKE"));
		assertTrue(lastPrivSet.contains("GETNESTED"));
		assertTrue(lastPrivSet.contains("NeedsRight4[ColorType]DomainValue[Color]"));
		
		UserPrivilege.clearLastprivSet();
		
		/**
		 * needed rights for setColor
		 */
		
		rad.setColor("RED");
		
		lastPrivSet = UserPrivilege.getPrivSet();
		assertTrue(lastPrivSet.size()==5);
		assertTrue(lastPrivSet.contains("BIKE"));
		assertTrue(lastPrivSet.contains("GETCOLTYPE"));
		assertTrue(lastPrivSet.contains("GETNESTED"));
		assertTrue(lastPrivSet.contains("NeedsRight4[ColorType]DomainValue[Color]"));
		assertTrue(lastPrivSet.contains("NeedsRight4[Color]DomainValue[RED]"));
		
		UserPrivilege.clearLastprivSet();
				
		/**
		 * needed rights for setColorShading
		 */
		
		rad.setColorShading("LightRED");
		
		lastPrivSet = UserPrivilege.getPrivSet();
		assertTrue(lastPrivSet.size()==6);
		assertTrue(lastPrivSet.contains("BIKE"));
		assertTrue(lastPrivSet.contains("GETCOLTYPE"));
		assertTrue(lastPrivSet.contains("GETNESTED"));
		assertTrue(lastPrivSet.contains("SETCOLSHADE"));
		assertTrue(lastPrivSet.contains("GETCOLOR"));
		assertTrue(lastPrivSet.contains("NeedsRight4[ColorType]DomainValue[Color]"));

		
		UserPrivilege.clearLastprivSet();
		
		/**
		 * needed rights for setNested
		 */
		
		rad.setNested(new NestedPropertyForPrivTest());
				
		lastPrivSet = UserPrivilege.getPrivSet();
		assertTrue(lastPrivSet.size()==4);
		assertTrue(lastPrivSet.contains("BIKE"));
		assertTrue(lastPrivSet.contains("GETNESTED"));
		assertTrue(lastPrivSet.contains("GETCOLTYPE"));
		assertTrue(lastPrivSet.contains("NeedsRight4[ColorType]DomainValue[Color]"));
		
		UserPrivilege.clearLastprivSet();
				
		/**
		 * needed rights for getNested
		 */
		
		NestedPropertyForPrivTest n = rad.getNested();

		lastPrivSet = UserPrivilege.getPrivSet();
		assertTrue(lastPrivSet.size()==2);
		assertTrue(lastPrivSet.contains("BIKE"));
		assertTrue(lastPrivSet.contains("GETNESTED"));
		
		UserPrivilege.clearLastprivSet();
		
		n.setNestedDomain("TEST");
		
		/**
		 * needed rights for setColorShading
		 */
		
		rad.setColorShading("LightRED");
		
		lastPrivSet = UserPrivilege.getPrivSet();
		assertTrue(lastPrivSet.size()==9);
		assertTrue(lastPrivSet.contains("BIKE"));
		assertTrue(lastPrivSet.contains("GETCOLTYPE"));
		assertTrue(lastPrivSet.contains("GETNESTED"));
		assertTrue(lastPrivSet.contains("SETCOLSHADE"));
		assertTrue(lastPrivSet.contains("GETCOLOR"));
		assertTrue(lastPrivSet.contains("NeedsRight4[ColorType]DomainValue[Color]"));
		assertTrue(lastPrivSet.contains("NestedPRIV1"));
		assertTrue(lastPrivSet.contains("NestedPRIV2"));
		assertTrue(lastPrivSet.contains("NestedPRIV3"));
		
		UserPrivilege.clearLastprivSet();
	}
}
