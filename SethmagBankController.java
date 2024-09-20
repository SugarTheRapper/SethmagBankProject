package com.example.Sethmag_BankProject;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/bank")
public class SethmagBankController {

    private final AccountService accountService;

    public SethmagBankController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/withdraw")
    public ResponseEntity<?> withdrawGet(@RequestParam String accountNum, @RequestParam double amount) {
        return withdraw(accountNum, amount);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdrawPost(@RequestParam String accountNum, @RequestParam double amount) {
        return withdraw(accountNum, amount);
    }

    private ResponseEntity<?> withdraw(String accountNum, double amount) {
        try {
            accountService.withdraw(accountNum, amount);
            Account account = accountService.getAccount(accountNum);
            return ResponseEntity.ok(String.format("Withdrawal successful. New balance: R%.2f", account.getBalance()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during withdrawal: " + e.getMessage());
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