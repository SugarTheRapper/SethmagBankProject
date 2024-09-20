package com.example.Sethmag_BankProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class SethmagBankProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(SethmagBankProjectApplication.class, args);
	}

	@Bean
	public AccountService accountService() {
		return new AccountServiceImpl();
	}
}

class Account {
	private String accountNum;
	private double balance;

	public Account(String accountNum, double balance) {
		if (accountNum == null || accountNum.isEmpty()) {
			throw new IllegalArgumentException("Account number cannot be null or empty.");
		}
		if (balance < 0) {
			throw new IllegalArgumentException("Balance cannot be negative.");
		}
		this.accountNum = accountNum;
		this.balance = balance;
	}

	public String getAccountNum() {
		return accountNum;
	}

	public double getBalance() {
		return balance;
	}

	public void withdraw(double amount) {
		if (amount > balance) {
			throw new IllegalArgumentException("Insufficient funds.");
		}
		balance -= amount;
	}
}

class SavingsAccount extends Account {
	private static final double MIN_BALANCE = 1000.0;

	public SavingsAccount(String accountNum, double balance) {
		super(accountNum, balance);
		if (balance < MIN_BALANCE) {
			throw new IllegalArgumentException("Initial deposit on Sethmag-Bank must be at least R1000.");
		}
	}

	@Override
	public void withdraw(double amount) {
		if (amount <= 0) {
			throw new IllegalArgumentException("Withdrawal amount from SethmagBank must be positive.");
		}
		double newBalance = getBalance() - amount;
		if (newBalance >= MIN_BALANCE) {
			super.withdraw(amount);
		} else {
			throw new IllegalArgumentException("Sorry! You can only withdraw an amount up to the nearest 1000 of your available balance because the minimum required balance for our accounts is R1000.");
		}
	}
}

class CurrentAccount extends Account {
	private static final double MAX_OVERDRAFT_LIMIT = 100000.0;
	private double overdraftLimit;

	public CurrentAccount(String accountNum, double balance, double overdraftLimit) {
		super(accountNum, balance);
		if (overdraftLimit > MAX_OVERDRAFT_LIMIT) {
			throw new IllegalArgumentException("On Sethmag-Bank Overdraft limit cannot exceed R100,000.");
		}
		this.overdraftLimit = overdraftLimit;
	}

	@Override
	public void withdraw(double amount) {
		if (amount <= 0) {
			throw new IllegalArgumentException("Withdrawal amount from Sethmag-Bank must be positive.");
		}
		double maxWithdrawal = getBalance() + overdraftLimit;
		if (amount <= maxWithdrawal) {
			super.withdraw(amount);
		} else {
			throw new IllegalArgumentException("Sorry! Withdrawal exceeds the available balance from Sethmag-Bank and overdraft limit.");
		}
	}
}

interface AccountService {
	void withdraw(String accountNum, double amountToWithdraw);
	Account getAccount(String accountNum);
}

class AccountServiceImpl implements AccountService {
	private Map<String, Account> accounts = new HashMap<>();

	public AccountServiceImpl() {
		accounts.put("123", new SavingsAccount("123", 5000.0));
		accounts.put("456", new CurrentAccount("456", 2000.0, 100000.0));
	}

	@Override
	public void withdraw(String accountNum, double amountToWithdraw) {
		Account account = accounts.get(accountNum);
		if (account == null) {
			throw new IllegalArgumentException("Account number not found from Sethmag-Bank. Please choose from '123' or '456'.");
		}
		account.withdraw(amountToWithdraw);
	}

	@Override
	public Account getAccount(String accountNum) {
		return accounts.get(accountNum);
	}
}

@RestController
@RequestMapping("/api/bank")
class sethmagBankController {

	private final AccountService accountService;

	public sethmagBankController(AccountService accountService) {
		this.accountService = accountService;
	}

	@PostMapping("/withdraw")
	public ResponseEntity<?> withdraw(@RequestParam String accountNum, @RequestParam double amount) {
		try {
			accountService.withdraw(accountNum, amount);
			Account account = accountService.getAccount(accountNum);
			return ResponseEntity.ok(String.format("Withdrawal successful. New balance: R%.2f", account.getBalance()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/balance")
	public ResponseEntity<?> getBalance(@RequestParam String accountNum) {
		Account account = accountService.getAccount(accountNum);
		if (account == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
		}
		return ResponseEntity.ok(String.format("Account balance: R%.2f", account.getBalance()));
	}
}