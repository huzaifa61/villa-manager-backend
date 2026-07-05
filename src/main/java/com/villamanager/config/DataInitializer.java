package com.villamanager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.villamanager.entity.*;
import com.villamanager.repository.*;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private VillaRepository villaRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PaymentCategoryRepository paymentCategoryRepository;
    @Autowired private ExpenseCategoryRepository expenseCategoryRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedVilla();
        seedUsers();
        seedCategories();
    }

    private void seedVilla() {
        if (villaRepository.count() == 0) {
            Villa villa = new Villa();
            villa.setName("Villa Manager Pro");
            villa.setLocation("Cairo, Egypt");
            villa.setDescription("Main managed villa");
            villa.setTotalApartments(0);
            villa.setIsActive(true);
            villa.setCreatedAt(LocalDateTime.now());
            villa.setUpdatedAt(LocalDateTime.now());
            villaRepository.save(villa);
            System.out.println("✅ Default villa created with ID: " + villa.getId());
        }
    }

    private void seedUsers() {
        if (!userRepository.existsByEmail("gm@villa.com")) {
            User gm = new User();
            gm.setEmail("gm@villa.com");
            gm.setFullName("General Manager");
            gm.setPassword(passwordEncoder.encode("password123"));
            gm.setRole(UserRole.GENERAL_MANAGER);
            gm.setIsActive(true);
            gm.setCreatedAt(LocalDateTime.now());
            userRepository.save(gm);
            System.out.println("✅ GM user created: gm@villa.com / password123");
        }
        if (!userRepository.existsByEmail("manager@villa.com")) {
            User vm = new User();
            vm.setEmail("manager@villa.com");
            vm.setFullName("Villa Manager");
            vm.setPassword(passwordEncoder.encode("password123"));
            vm.setRole(UserRole.VILLA_MANAGER);
            vm.setIsActive(true);
            vm.setCreatedAt(LocalDateTime.now());
            userRepository.save(vm);
        }
    }

    private void seedCategories() {
        if (paymentCategoryRepository.count() == 0) {
            String[] paymentCats = {"Rent", "Maintenance Fee", "Utility", "Parking", "Other"};
            for (String name : paymentCats) {
                PaymentCategory cat = new PaymentCategory();
                cat.setName(name);
                cat.setIsActive(true);
                paymentCategoryRepository.save(cat);
            }
            System.out.println("✅ Payment categories seeded");
        }
        if (expenseCategoryRepository.count() == 0) {
            String[] expenseCats = {"Maintenance", "Utilities", "Cleaning", "Security", "Management", "Other"};
            for (String name : expenseCats) {
                ExpenseCategory cat = new ExpenseCategory();
                cat.setName(name);
                cat.setIsActive(true);
                expenseCategoryRepository.save(cat);
            }
            System.out.println("✅ Expense categories seeded");
        }
    }
}
