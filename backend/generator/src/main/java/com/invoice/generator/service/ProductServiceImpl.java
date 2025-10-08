package com.invoice.generator.service;

import com.invoice.generator.dto.ProductDto;
import com.invoice.generator.model.Product;
import com.invoice.generator.model.ProductCategory;
import com.invoice.generator.model.User;
import com.invoice.generator.repository.ProductCategoryRepository;
import com.invoice.generator.repository.ProductRepository;
import com.invoice.generator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductCategoryRepository categoryRepository;

    public List<ProductDto> getProductsByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Product> products = productRepository.findAllByShop(user.getShop());
        
        // Convert the list of Product entities to a list of ProductDtos
        return products.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public Product createProduct(ProductDto productDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        ProductCategory category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product newProduct = new Product();
        newProduct.setName(productDto.getName());
        newProduct.setSellingPrice(productDto.getSellingPrice());
        newProduct.setCategory(category);
        newProduct.setShop(user.getShop());

        return productRepository.save(newProduct);
    }

    // Helper method to map a Product entity to a ProductDto
    private ProductDto mapToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSellingPrice(product.getSellingPrice());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getCategoryName());
        dto.setGstPercentage(product.getCategory().getGstPercentage());
        return dto;
    }
}