package com.revature.revshop.service;

import com.revature.revshop.dto.CategoryDTO;
import com.revature.revshop.exception.InvalidInputException;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.model.Category;
import com.revature.revshop.model.Product;
import com.revature.revshop.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;

    @InjectMocks private CategoryService categoryService;



    private Category electronics;
    private Category phones;
    private CategoryDTO electronicsDTO;

    @BeforeEach
    void setUp() {
        // Parent category
        electronics = new Category();
        electronics.setCategoryId(1L);
        electronics.setName("Electronics");
        electronics.setDescription("Electronic gadgets and devices");

        phones = new Category();
        phones.setCategoryId(2L);
        phones.setName("Phones");
        phones.setDescription("Mobile phones and accessories");
        phones.setParentCategory(electronics);

        // DTO
        electronicsDTO = new CategoryDTO();
        electronicsDTO.setName("Electronics");
        electronicsDTO.setDescription("Electronic gadgets and devices");
    }



    @Test
    void createCategory_shouldSaveAndReturnDTO_whenNameIsNew() {
        when(categoryRepository.existsByName("Electronics")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(electronics);

        CategoryDTO result = categoryService.createCategory(electronicsDTO);

        assertThat(result).isNotNull();
        assertThat(result.getCategoryId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Electronics");
        assertThat(result.getDescription()).isEqualTo("Electronic gadgets and devices");

        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_shouldThrowInvalidInputException_whenNameAlreadyExists() {
        when(categoryRepository.existsByName("Electronics")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(electronicsDTO))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Category already exists");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void createCategory_childCategory_shouldBeSavedIndependently() {
        CategoryDTO phonesDTO = new CategoryDTO();
        phonesDTO.setName("Phones");
        phonesDTO.setDescription("Mobile phones and accessories");

        when(categoryRepository.existsByName("Phones")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(phones);

        CategoryDTO result = categoryService.createCategory(phonesDTO);

        assertThat(result.getName()).isEqualTo("Phones");

        verify(categoryRepository, times(1)).save(any(Category.class));
    }



    @Test
    void getAllCategories_shouldReturnAllCategoriesAsDTOs() {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(electronics, phones));

        List<CategoryDTO> results = categoryService.getAllCategories();

        assertThat(results).hasSize(2);
        assertThat(results).extracting(CategoryDTO::getName)
                .containsExactlyInAnyOrder("Electronics", "Phones");
    }

    @Test
    void getAllCategories_shouldReturnEmptyList_whenNoCategoriesExist() {
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

        List<CategoryDTO> results = categoryService.getAllCategories();

        assertThat(results).isEmpty();
    }



    @Test
    void getRootCategories_shouldReturnOnlyRootCategories() {
        when(categoryRepository.findByParentCategoryIsNull()).thenReturn(List.of(electronics));

        List<CategoryDTO> results = categoryService.getRootCategories();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Electronics");
        assertThat(results.get(0).getParentCategoryId()).isNull();
    }

    @Test
    void getSubCategories_shouldReturnSubcategories_whenParentExists() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.findByParentCategory_CategoryId(1L)).thenReturn(List.of(phones));

        List<CategoryDTO> results = categoryService.getSubCategories(1L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Phones");
        assertThat(results.get(0).getParentCategoryId()).isEqualTo(1L);
    }

    @Test
    void getSubCategories_shouldThrowException_whenParentDoesNotExist() {
        when(categoryRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.getSubCategories(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Parent category not found");
    }



    @Test
    void getCategoryById_shouldReturnDTO_whenCategoryFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronics));

        CategoryDTO result = categoryService.getCategoryById(1L);

        assertThat(result.getCategoryId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Electronics");
    }

    @Test
    void getCategoryById_shouldThrowResourceNotFoundException_whenCategoryNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
    }



    @Test
    void updateCategory_shouldUpdateNameAndDescription_whenNameIsNotTaken() {
        CategoryDTO updateDTO = new CategoryDTO();
        updateDTO.setName("Consumer Electronics");
        updateDTO.setDescription("Updated description");

        Category updated = new Category();
        updated.setCategoryId(1L);
        updated.setName("Consumer Electronics");
        updated.setDescription("Updated description");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronics));
        when(categoryRepository.existsByName("Consumer Electronics")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(updated);

        CategoryDTO result = categoryService.updateCategory(1L, updateDTO);

        assertThat(result.getName()).isEqualTo("Consumer Electronics");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void updateCategory_shouldSucceed_whenNameRemainsTheSame() {

        CategoryDTO sameNameDTO = new CategoryDTO();
        sameNameDTO.setName("Electronics");
        sameNameDTO.setDescription("Updated description only");

        Category updatedSameName = new Category();
        updatedSameName.setCategoryId(1L);
        updatedSameName.setName("Electronics");
        updatedSameName.setDescription("Updated description only");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronics));

        when(categoryRepository.save(any(Category.class))).thenReturn(updatedSameName);

        CategoryDTO result = categoryService.updateCategory(1L, sameNameDTO);

        assertThat(result.getName()).isEqualTo("Electronics");
        assertThat(result.getDescription()).isEqualTo("Updated description only");
    }

    @Test
    void updateCategory_shouldThrowInvalidInputException_whenNewNameAlreadyTakenByAnotherCategory() {

        CategoryDTO conflictDTO = new CategoryDTO();
        conflictDTO.setName("Phones");
        conflictDTO.setDescription("Any description");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronics));

        when(categoryRepository.existsByName("Phones")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.updateCategory(1L, conflictDTO))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Category name already exists");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_shouldThrowResourceNotFoundException_whenCategoryNotFound() {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Anything");
        dto.setDescription("Desc");

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(999L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
    }



    @Test
    void deleteCategory_shouldDelete_whenCategoryFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronics));
        doNothing().when(categoryRepository).delete(electronics);

        assertThatCode(() -> categoryService.deleteCategory(1L))
                .doesNotThrowAnyException();

        verify(categoryRepository, times(1)).delete(electronics);
    }

    @Test
    void deleteCategory_shouldThrowResourceNotFoundException_whenCategoryNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");

        verify(categoryRepository, never()).delete(any());
    }



    @Test
    void categoryHierarchy_parentCategoryShouldHoldProducts() {

        Product product = new Product();
        product.setProductId(1L);
        product.setName("Galaxy S24");
        product.setCategory(phones);

        phones.setProducts(List.of(product));


        assertThat(phones.getProducts()).hasSize(1);
        assertThat(phones.getProducts().get(0).getName()).isEqualTo("Galaxy S24");
        assertThat(phones.getProducts().get(0).getCategory().getName()).isEqualTo("Phones");
    }

    @Test
    void categoryHierarchy_parentCategoryDTO_shouldReflectCorrectId() {

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(phones));

        CategoryDTO result = categoryService.getCategoryById(2L);

        assertThat(result.getCategoryId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Phones");
        // This categoryId would be used in ProductDTO.setCategoryId(...)
    }
}
