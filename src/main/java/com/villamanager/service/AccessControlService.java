package com.villamanager.service;

import com.villamanager.entity.User;
import com.villamanager.entity.UserRole;
import com.villamanager.exception.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {

    @Autowired
    private CurrentUserService currentUserService;

    public User currentUser() {
        return currentUserService.getCurrentUser();
    }

    public void requireVillaRead(Long villaId) {
        User user = currentUser();
        if (user.getRole() == UserRole.GENERAL_MANAGER || villaId.equals(user.getVillaId())) return;
        throw new AccessDeniedException("You do not have access to this villa");
    }

    public void requireVillaManage(Long villaId) {
        User user = currentUser();
        if (user.getRole() == UserRole.GENERAL_MANAGER) return;
        if (user.getRole() == UserRole.VILLA_MANAGER && villaId.equals(user.getVillaId())) return;
        throw new AccessDeniedException("Only managers can change this villa");
    }

    public void requireFinancialManage(Long villaId) {
        requireVillaManage(villaId);
    }

    public void requireGeneralManager() {
        if (currentUser().getRole() != UserRole.GENERAL_MANAGER) {
            throw new AccessDeniedException("Only the General Manager can perform this action");
        }
    }

    public void requireVendorManage() {
        User user = currentUser();
        if (user.getRole() == UserRole.GENERAL_MANAGER || user.getRole() == UserRole.VILLA_MANAGER) return;
        throw new AccessDeniedException("Viewers cannot manage vendors");
    }

    public void requireServiceManage(Long villaId) {
        // Viewers can create/update their own service requests, managers can do everything
        User user = currentUser();
        if (user.getRole() == UserRole.GENERAL_MANAGER) return;
        if (user.getRole() == UserRole.VILLA_MANAGER && villaId.equals(user.getVillaId())) return;
        if (user.getRole() == UserRole.VIEWER && villaId.equals(user.getVillaId())) return;
        throw new AccessDeniedException("You do not have access to manage service requests for this villa");
    }
}
