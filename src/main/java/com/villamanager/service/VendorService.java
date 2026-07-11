package com.villamanager.service;

import com.villamanager.entity.User;
import com.villamanager.entity.UserRole;
import com.villamanager.entity.Vendor;
import com.villamanager.entity.Villa;
import com.villamanager.exception.AccessDeniedException;
import com.villamanager.exception.ResourceNotFoundException;
import com.villamanager.repository.VendorRepository;
import com.villamanager.repository.VillaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VendorService {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private VillaRepository villaRepository;

    @Autowired
    private AccessControlService accessControlService;

    public List<Vendor> listForCurrentUser() {
        User user = accessControlService.currentUser();
        if (user.getRole() == UserRole.GENERAL_MANAGER) {
            return vendorRepository.findAll();
        }
        String region = resolveUserVillaRegion(user);
        if (region == null) {
            return List.of();
        }
        return vendorRepository.findByRegionIgnoreCase(region);
    }

    public Vendor create(Vendor body) {
        accessControlService.requireVendorManage();
        User user = accessControlService.currentUser();

        Vendor vendor = new Vendor();
        vendor.setId(null);
        applyBody(vendor, body);
        vendor.setRegion(resolveRegionForWrite(user, body.getRegion()));
        vendor.setIsActive(body.getIsActive() != null ? body.getIsActive() : true);
        return vendorRepository.save(vendor);
    }

    public Vendor update(Long vendorId, Vendor body) {
        accessControlService.requireVendorManage();
        User user = accessControlService.currentUser();
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        requireVendorInScope(user, vendor);
        applyBody(vendor, body);
        if (user.getRole() == UserRole.GENERAL_MANAGER) {
            vendor.setRegion(resolveRegionForWrite(user, body.getRegion()));
        }
        vendor.setIsActive(body.getIsActive() != null ? body.getIsActive() : true);
        return vendorRepository.save(vendor);
    }

    public void delete(Long vendorId) {
        accessControlService.requireVendorManage();
        User user = accessControlService.currentUser();
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        requireVendorInScope(user, vendor);
        vendorRepository.delete(vendor);
    }

    private void applyBody(Vendor vendor, Vendor body) {
        vendor.setName(body.getName());
        vendor.setContactPerson(body.getContactPerson());
        vendor.setEmail(body.getEmail());
        vendor.setPhoneNumber(body.getPhoneNumber());
        vendor.setAddress(body.getAddress());
        vendor.setServiceType(body.getServiceType());
    }

    private String resolveRegionForWrite(User user, String requestedRegion) {
        if (user.getRole() == UserRole.VILLA_MANAGER) {
            String region = resolveUserVillaRegion(user);
            if (region == null) {
                throw new IllegalArgumentException("Your villa must have a location/region before managing vendors");
            }
            return region;
        }
        String region = normalizeRegion(requestedRegion);
        if (region == null) {
            throw new IllegalArgumentException("Location/region is required");
        }
        return region;
    }

    private void requireVendorInScope(User user, Vendor vendor) {
        if (user.getRole() == UserRole.GENERAL_MANAGER) {
            return;
        }
        String userRegion = resolveUserVillaRegion(user);
        if (userRegion == null || vendor.getRegion() == null
                || !userRegion.equalsIgnoreCase(vendor.getRegion().trim())) {
            throw new AccessDeniedException("You can only manage vendors in your area");
        }
    }

    private String resolveUserVillaRegion(User user) {
        if (user.getVillaId() == null) {
            return null;
        }
        return villaRepository.findById(user.getVillaId())
                .map(Villa::getRegion)
                .map(this::normalizeRegion)
                .orElse(null);
    }

    private String normalizeRegion(String region) {
        if (region == null) {
            return null;
        }
        String trimmed = region.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
