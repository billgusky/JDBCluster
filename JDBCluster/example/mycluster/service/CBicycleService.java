package mycluster.service;

import mycluster.CBicycle;

import org.jdbcluster.metapersistence.annotation.PrivilegesMethod;
import org.jdbcluster.metapersistence.annotation.PrivilegesService;
import org.jdbcluster.service.PrivilegedService;
import org.jdbcluster.service.ServiceBase;

@PrivilegesService(required="BIKESERVCIE")
public class CBicycleService extends ServiceBase implements ICBicycleService, PrivilegedService {

	@PrivilegesMethod(required="BIKEPAINT")
	public void makeBicycleRED(CBicycle bike) {
	}

}
