 package com.wks.caseengine.service;

 import java.util.List;

 import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.message.vm.AOPMessageVM;

 public interface NormTransactionsService   {
	
 	
    
    public AOPMessageVM getNormTransactions(String plantId,String year,String screen);

 }
