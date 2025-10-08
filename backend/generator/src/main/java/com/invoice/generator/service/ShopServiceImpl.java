package com.invoice.generator.service;

import com.invoice.generator.dto.ShopSettingsDto;
import com.invoice.generator.model.Shop;
import com.invoice.generator.model.User;
import com.invoice.generator.repository.ShopRepository;
import com.invoice.generator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ShopServiceImpl {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    // This method now returns a DTO
    public ShopSettingsDto getShopSettingsByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return mapToDto(user.getShop());
    }

    public ShopSettingsDto updateShopSettings(ShopSettingsDto settingsDto, String username) { // Return DTO
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Shop shopToUpdate = user.getShop();
        shopToUpdate.setShopName(settingsDto.getShopName());
        shopToUpdate.setGstin(settingsDto.getGstin());
        shopToUpdate.setAddress(settingsDto.getAddress());

        Shop updatedShop = shopRepository.save(shopToUpdate); // Save the shop
        return mapToDto(updatedShop); // Map the final result to a DTO and return it
    }

    // Helper method to convert a Shop entity to a DTO
    private ShopSettingsDto mapToDto(Shop shop) {
        ShopSettingsDto dto = new ShopSettingsDto();
        dto.setShopName(shop.getShopName());
        dto.setGstin(shop.getGstin());
        dto.setAddress(shop.getAddress());
        dto.setLogoPath(shop.getLogoPath()); // <-- ADD THIS LINE
        return dto;
}
}