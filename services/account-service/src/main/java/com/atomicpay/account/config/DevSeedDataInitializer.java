package com.atomicpay.account.config;

import com.atomicpay.account.domain.AccountStatus;
import com.atomicpay.account.domain.AccountType;
import com.atomicpay.account.entity.AccountEntity;
import com.atomicpay.account.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Profile("!test")
public class DevSeedDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevSeedDataInitializer.class);

    private static final UUID ALICE_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID BOB_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CHARLIE_USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private final AccountRepository accountRepository;

    public DevSeedDataInitializer(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(String... args) {
        if (accountRepository.count() == 0) {
            log.info("Seeding initial development accounts for Alice, Bob, and Charlie...");

            // Alice Account: ₹100,000.0000
            AccountEntity aliceAccount = AccountEntity.builder()
                    .id(UUID.fromString("a1111111-1111-1111-1111-111111111111"))
                    .userId(ALICE_USER_ID)
                    .accountNumber("ACC1000000001")
                    .currency("INR")
                    .accountType(AccountType.WALLET)
                    .status(AccountStatus.ACTIVE)
                    .currentBalance(new BigDecimal("100000.0000"))
                    .build();

            // Bob Account: ₹20,000.0000
            AccountEntity bobAccount = AccountEntity.builder()
                    .id(UUID.fromString("b2222222-2222-2222-2222-222222222222"))
                    .userId(BOB_USER_ID)
                    .accountNumber("ACC1000000002")
                    .currency("INR")
                    .accountType(AccountType.WALLET)
                    .status(AccountStatus.ACTIVE)
                    .currentBalance(new BigDecimal("20000.0000"))
                    .build();

            // Charlie Account: ₹5,000.0000
            AccountEntity charlieAccount = AccountEntity.builder()
                    .id(UUID.fromString("c3333333-3333-3333-3333-333333333333"))
                    .userId(CHARLIE_USER_ID)
                    .accountNumber("ACC1000000003")
                    .currency("INR")
                    .accountType(AccountType.WALLET)
                    .status(AccountStatus.ACTIVE)
                    .currentBalance(new BigDecimal("5000.0000"))
                    .build();

            accountRepository.save(aliceAccount);
            accountRepository.save(bobAccount);
            accountRepository.save(charlieAccount);

            log.info("Development accounts successfully seeded: Alice (ACC1000000001 = ₹100,000), Bob (ACC1000000002 = ₹20,000), Charlie (ACC1000000003 = ₹5,000)");
        }
    }
}
