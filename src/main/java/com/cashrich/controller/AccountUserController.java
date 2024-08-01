package com.cashrich.controller;


import com.cashrich.model.*;
import com.cashrich.service.AccountUserService;
import com.cashrich.service.ThirdPartyAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AccountUserController {

    @Autowired
    private final AccountUserService accountUserService;

    @Autowired
    private final ThirdPartyAPIService thirdPartyAPIService;

    public AccountUserController(AccountUserService accountUserService, ThirdPartyAPIService thirdPartyAPIService) {
        this.accountUserService = accountUserService;
        this.thirdPartyAPIService = thirdPartyAPIService;
    }


    @GetMapping("/users/{userId}")
    public ResponseEntity<AccountUser> getUserByUsername(@PathVariable Long userId) {
        AccountUser user = accountUserService.findByUsername(userId);
        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("sign-up")
    public ResponseEntity<AccountUserResponse> createProfile(@RequestBody AccountUserProfile accountUserProfile) {
        try {
            AccountUserResponse response = accountUserService.createUser(accountUserProfile);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new AccountUserResponse("Failed to sign-up = " + e.getMessage(),null), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<AccountUserResponse> updateUserDetails(@PathVariable Long userId, @RequestBody UpdateAccountUserDetail updateAccountUserDetail) {
        AccountUserResponse response = accountUserService.updateUserDetails(userId, updateAccountUserDetail);
        if (response.getMessage().equals("User updated successfully")) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("users/update-password")
    public ResponseEntity<AccountUserResponse> updatePassword(@RequestBody UpdateAccountUserProfilePassword updateAccountUserProfilePassword) {
        AccountUserResponse response = accountUserService.updateUserPassword(updateAccountUserProfilePassword);
        if (response.getMessage().equals("User updated successfully")) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/coin")
    public ResponseEntity<String> getCryptoData(@RequestParam String userId) {
        try {
            String response = thirdPartyAPIService.callCoinApi(userId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>( e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
