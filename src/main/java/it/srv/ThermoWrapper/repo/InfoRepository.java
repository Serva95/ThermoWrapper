package it.srv.ThermoWrapper.repo;

import it.srv.ThermoWrapper.model.Info;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface InfoRepository extends CrudRepository<Info, Short> {
    Info findFirstByOrderByIdDesc();

    Info findFirstByOrderByLastupdateDesc();

}