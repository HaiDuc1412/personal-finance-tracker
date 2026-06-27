package com.haiduc.personalfinancetracker.category;

import com.haiduc.personalfinancetracker.category.dto.CategoryRequest;
import com.haiduc.personalfinancetracker.category.dto.CategoryResponse;
import com.haiduc.personalfinancetracker.common.enums.TransactionType;
import com.haiduc.personalfinancetracker.common.exception.AppException;
import com.haiduc.personalfinancetracker.transaction.TransactionRepository;
import com.haiduc.personalfinancetracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public List<CategoryResponse> getCategories(User currentUser, TransactionType type) {
        UUID userId = currentUser.getId();

        List<Category> categories = (type != null)
                ? categoryRepository.findAllAvailableForUserByType(userId, type)
                : categoryRepository.findAllAvailableForUser(userId);

        return categories.stream()
                .map(c -> toResponse(c, userId))
                .toList();
    }

    public CategoryResponse createCategory(User currentUser, CategoryRequest request) {
        boolean nameExists = categoryRepository
                .findAllAvailableForUser(currentUser.getId())
                .stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(request.getName()));

        if (nameExists) {
            throw new AppException("Category name already exists", HttpStatus.CONFLICT);
        }

        Category category = Category.builder()
                .name(request.getName())
                .type(request.getType())
                .user(currentUser)
                .isDefault(false)
                .build();

        return toResponse(categoryRepository.save(category), currentUser.getId());
    }

    public CategoryResponse updateCategory(User currentUser, UUID categoryId, CategoryRequest request) {
        Category category = getOwnedCategoryOrThrow(currentUser, categoryId);

        boolean nameExists = categoryRepository
                .findAllAvailableForUser(currentUser.getId())
                .stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(request.getName())
                        && !c.getId().equals(categoryId));

        if (nameExists) {
            throw new AppException("Category name already exists", HttpStatus.CONFLICT);
        }

        category.setName(request.getName());
        category.setType(request.getType());

        return toResponse(categoryRepository.save(category), currentUser.getId());
    }

    @Transactional
    public void deleteCategory(User currentUser, UUID categoryId) {
        Category category = getOwnedCategoryOrThrow(currentUser, categoryId);

        Category uncategorized = categoryRepository
                .findByNameAndUserIsNull("Uncategorized")
                .orElseThrow(() -> new AppException(
                        "System category 'Uncategorized' not found",
                        HttpStatus.INTERNAL_SERVER_ERROR));

        transactionRepository.reassignCategory(categoryId, uncategorized.getId());

        categoryRepository.delete(category);
    }

    private Category getOwnedCategoryOrThrow(User currentUser, UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException("Category not found", HttpStatus.NOT_FOUND));

        if (category.getUser() == null || !category.getUser().getId().equals(currentUser.getId())) {
            throw new AppException("You don't have permission to modify this category", HttpStatus.FORBIDDEN);
        }

        return category;
    }

    private CategoryResponse toResponse(Category category, UUID currentUserId) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .isDefault(category.isDefault())
                .isOwner(category.getUser() != null
                        && category.getUser().getId().equals(currentUserId))
                .build();
    }
}
