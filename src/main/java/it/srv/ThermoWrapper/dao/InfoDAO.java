package it.srv.ThermoWrapper.dao;

import it.srv.ThermoWrapper.model.Info;
import it.srv.ThermoWrapper.repo.InfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InfoDAO {
    @Autowired
    private InfoRepository repo;

    public Info findFirst(){ return repo.findFirstByOrderByIdDesc(); }

    public Info findLastTemporal(){ return repo.findFirstByOrderByLastupdateDesc(); }

    public Info save(Info info) { return repo.save(info); }

    public Info get(short id) { return repo.findById(id).orElse(null); }

    public void delete(short id) { repo.deleteById(id); }

}
