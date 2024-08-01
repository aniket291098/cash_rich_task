package com.cashrich.service;

import com.cashrich.entity.AccountUserEntity;
import com.cashrich.entity.AccountUserProfileEntity;
import com.cashrich.model.*;
import com.cashrich.repository.AccountUserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Log4j2
public class AccountUserService {

    @Autowired
    private final AccountUserRepository accountUserRepository;

    public AccountUserService(AccountUserRepository accountUserRepository) {
        this.accountUserRepository = accountUserRepository;
    }


    public AccountUser findByUsername(Long userId) {
        Optional<AccountUserEntity> accountUserEntity = accountUserRepository.findById(userId);
        if (accountUserEntity.isEmpty()) {
            return null;
        }

        AccountUser accountUser = new AccountUser();
        accountUser.setFirstName(accountUserEntity.get().getFirstName());
        accountUser.setLastName(accountUserEntity.get().getLastName());
        accountUser.setMobile(accountUserEntity.get().getMobile());
        accountUser.setEmail(accountUserEntity.get().getEmail());

        AccountUserProfile accountUserProfile = new AccountUserProfile();
        if (accountUserEntity.get().getAccountUserProfile() != null) {
            accountUserProfile.setUsername(accountUserEntity.get().getAccountUserProfile().getUsername());
            accountUserProfile.setPassword(accountUserEntity.get().getAccountUserProfile().getPassword());
        }
        accountUser.setAccountUserProfile(accountUserProfile);

        return accountUser;
    }


    public AccountUserResponse createUser(AccountUserProfile accountUserProfile) {

        if (accountUserProfile == null ||
                accountUserProfile.getUsername() == null ||
                accountUserProfile.getUsername().trim().isEmpty() ||
                accountUserProfile.getPassword() == null ||
                accountUserProfile.getPassword().trim().isEmpty()) {
            return new AccountUserResponse("Username and password must not be blank", null);
        }


        if (accountUserRepository.existsByAccountUserProfile_Username(accountUserProfile.getUsername())) {
            return new AccountUserResponse("Username already in use", null);
        }

        AccountUserProfileEntity profile = new AccountUserProfileEntity();
        profile.setUsername(accountUserProfile.getUsername());
        profile.setPassword(accountUserProfile.getPassword());

        AccountUserEntity user = new AccountUserEntity();
        user.setEmail("example@example.com");
        user.setMobile("0000000000");
        user.setAccountUserProfile(profile);

        accountUserRepository.save(user);

        return new AccountUserResponse("User Profile successfully created", user.getId());
    }

    public AccountUserResponse updateUserDetails(Long userId, UpdateAccountUserDetail updateAccountUserDetail) {
        Optional<AccountUserEntity> existingUser = Optional.ofNullable(accountUserRepository.findById(userId).orElse(null));

        if (existingUser.isEmpty()) {
            return new AccountUserResponse("User not found", null);
        }

        if (accountUserRepository.existsByEmail(updateAccountUserDetail.getEmail()) && !existingUser.get().getEmail().equals(updateAccountUserDetail.getEmail())) {
            return new AccountUserResponse("Email already in use", null);
        }

        if (accountUserRepository.existsByMobile(updateAccountUserDetail.getMobile()) && !existingUser.get().getMobile().equals(updateAccountUserDetail.getMobile())) {
            return new AccountUserResponse("Mobile number already in use", null);
        }

        existingUser.get().setFirstName(updateAccountUserDetail.getFirstName());
        existingUser.get().setLastName(updateAccountUserDetail.getLastName());
        existingUser.get().setEmail(updateAccountUserDetail.getEmail());
        existingUser.get().setMobile(updateAccountUserDetail.getMobile());

        accountUserRepository.save(existingUser.get());

        return new AccountUserResponse("Profile details updated successfully. Your profile is now complete.", existingUser.get().getId());
    }


    public AccountUserResponse updateUserPassword(UpdateAccountUserProfilePassword updateAccountUserProfilePassword) {

        Optional<AccountUserEntity> optionalUser = accountUserRepository.findByEmailOrMobileAndAccountUserProfile_Username(
                updateAccountUserProfilePassword.getEmail(),
                updateAccountUserProfilePassword.getMobile(),
                updateAccountUserProfilePassword.getUsername()
        );

        if (!optionalUser.isPresent()) {
            return new AccountUserResponse("User not found. Please check the email or phone number.", null);
        }

        AccountUserEntity existingUser = optionalUser.get();

        if (existingUser.getAccountUserProfile() != null &&
                existingUser.getAccountUserProfile().getUsername().equals(updateAccountUserProfilePassword.getUsername())) {
            existingUser.getAccountUserProfile().setPassword(updateAccountUserProfilePassword.getPassword());
        } else {
            return new AccountUserResponse("Username does not match the provided details.", null);
        }

        accountUserRepository.save(existingUser);

        return new AccountUserResponse("Password updated successfully", existingUser.getId());
    }

}
