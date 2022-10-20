package com.wks.caseengine.record.type;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.repository.RecordTypeRepository;

@Component
public class RecordTypeServiceImpl implements RecordTypeService {

	@Autowired
	private RecordTypeRepository repository;

	@Override
	public void save(RecordType recordType) throws Exception {
		repository.save(recordType);
	}

	@Override
	public RecordType get(String id) throws Exception {
		return repository.get(id);
	}

	@Override
	public List<RecordType> find() throws Exception {
		return repository.find();
	}

	@Override
	public void delete(String id) throws Exception {
		repository.delete(id);
	}

	@Override
	public void update(final String id, final RecordType recordType) throws Exception {
		repository.update(id, recordType);
	}

}
