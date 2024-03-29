package com.tcs.RBRBankSpring.services;

import com.tcs.RBRBankSpring.controllers.AccountController;
import com.tcs.RBRBankSpring.controllers.LogTransactionsController;
import com.tcs.RBRBankSpring.models.Account;
import com.tcs.RBRBankSpring.models.Investment;
import com.tcs.RBRBankSpring.models.TransactionType;
import com.tcs.RBRBankSpring.repositories.InvestmentRepository;
import com.tcs.RBRBankSpring.request.InvestmentRequest;
import com.tcs.RBRBankSpring.request.TransferRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.ManyToOne;
import java.time.LocalDate;

@Service
public class InvestmentService {
    private InvestmentRepository investmentRepository;
    private LogTransactionsController logTransactionsController;
    private AccountController accountController;

    @Autowired
    public InvestmentService(InvestmentRepository investmentRepository, LogTransactionsController logTransactionsController,
                             AccountController accountController) {
        this.investmentRepository = investmentRepository;
        this.logTransactionsController = logTransactionsController;
        this.accountController = accountController;
    }

    public boolean createInvestment(InvestmentRequest investmentRequest) {
        Account account = validateInvestment(investmentRequest);
        if(null != account) {
            Investment invest = new Investment();
            invest.setValue(investmentRequest.getValue());
            invest.setUserAccount(account);
            invest.setInvestmentType(investmentRequest.getInvestmentName());
            LocalDate data = LocalDate.now();
            switch (investmentRequest.getInvestmentName().toLowerCase()) {
                case "cdi": invest.setExpiry(data.plusYears(1));
                        invest.setProfitabilityRate(6F);
                        break;
                case "ipca": invest.setExpiry(data.plusYears(1));
                        invest.setProfitabilityRate(7F);
                        break;
                case "poupanca": invest.setExpiry(data.plusYears(100));
                        invest.setProfitabilityRate(4F);
                        break;
                default: return false;
            }
            TransferRequest transferRequest = new TransferRequest();
            transferRequest.setReceiverId(000001);
            transferRequest.setSenderId(investmentRequest.getAccount().getNumberAccount());
            transferRequest.setValue((double) investmentRequest.getValue());

            accountController.doTransfer(transferRequest);
            investmentRepository.save(invest);
            logTransactionsController.newLog(TransactionType.INVESTMENT, account.getId(),
                    "Investimento "+invest.getInvestmentType()+" feito por "+account.getNumberAccount());
            return true;
        }
        return false;
    }

    private Account validateInvestment(InvestmentRequest investment) {
        if(investment.getValue() <= 0 || investment.getValue() < investment.getMinimumValue())
            return null;

        Account account = accountController.findByAccount(investment.getAccount().getNumberAccount());
        if(account.getBalance() < investment.getValue())
            return null;

        return account;
    }
}
