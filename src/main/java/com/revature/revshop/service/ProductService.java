package com.revature.revshop.service;

import com.revature.revshop.dto.ProductDTO;
import com.revature.revshop.exception.InvalidInputException;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.exception.UserNotFoundException;
import com.revature.revshop.model.Category;
import com.revature.revshop.model.Product;
import com.revature.revshop.model.Seller;
import com.revature.revshop.repository.CategoryRepository;
import com.revature.revshop.repository.ProductRepository;
import com.revature.revshop.repository.SellerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

import com.revature.revshop.model.ProductVideo;
import com.revature.revshop.repository.ProductVideoRepository;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private static final String PRODUCT_NOT_FOUND = "Product not found";
    private static final String CATEGORY_NOT_FOUND = "Category not found";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SellerRepository sellerRepository;
    private final ProductVideoRepository productVideoRepository;

    public ProductService(ProductRepository productRepository,
            CategoryRepository categoryRepository,
            SellerRepository sellerRepository,
            ProductVideoRepository productVideoRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.sellerRepository = sellerRepository;
        this.productVideoRepository = productVideoRepository;
    }

    public ProductDTO createProduct(ProductDTO dto) {
        log.info("Creating product name={}", dto.getName());

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND));

        Seller seller = sellerRepository.findById(dto.getSellerId())
                .orElseThrow(() -> new UserNotFoundException("Seller not found"));

        Product product = new Product();
        mapDtoToEntity(dto, product);
        product.setCategory(category);
        product.setSeller(seller);

        Product saved = productRepository.save(product);

        return convertToDTO(saved);
    }

    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    public ProductDTO getProductById(Long id) {
        log.info("Fetching product id={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));

        return convertToDTO(product);
    }

    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        log.info("Updating product id={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));

        mapDtoToEntity(dto, product);

        Product updated = productRepository.save(product);

        return convertToDTO(updated);
    }

    public void deleteProduct(Long id) {
        log.info("Deleting product id={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));

        productRepository.delete(product);
    }

    public Page<ProductDTO> searchProducts(String keyword, Pageable pageable) {
        log.info("Searching products keyword={}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new InvalidInputException("Search keyword is required");
        }

        return productRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable)
                .map(this::convertToDTO);
    }

    public Page<ProductDTO> filterProducts(String keyword, Double minPrice, Double maxPrice, Long categoryId,
            Integer minRating, Integer minDiscount, java.util.Map<String, String> dynamicFilters, Pageable pageable) {

        org.springframework.data.jpa.domain.Specification<Product> spec = org.springframework.data.jpa.domain.Specification
                .where((root, query, cb) -> cb.conjunction());

        if (keyword != null && !keyword.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")));
        }

        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("sellingPrice"), minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("sellingPrice"), maxPrice));
        }

        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> {
                jakarta.persistence.criteria.Join<Product, Category> categoryJoin = root.join("category");
                jakarta.persistence.criteria.Path<Category> parentCat = categoryJoin.get("parentCategory");
                return cb.or(
                        cb.equal(categoryJoin.get("categoryId"), categoryId),
                        cb.and(cb.isNotNull(parentCat), cb.equal(parentCat.get("categoryId"), categoryId)));
            });
        }

        if (minRating != null) {
            spec = spec.and((root, query, cb) -> {
                jakarta.persistence.criteria.Subquery<Double> subquery = query.subquery(Double.class);
                jakarta.persistence.criteria.Root<com.revature.revshop.model.Review> reviewRoot = subquery
                        .from(com.revature.revshop.model.Review.class);
                subquery.select(cb.avg(reviewRoot.get("rating").as(Double.class)))
                        .where(cb.equal(reviewRoot.get("product"), root));
                return cb.greaterThanOrEqualTo(cb.coalesce(subquery, 0.0), minRating.doubleValue());
            });
        }

        if (minDiscount != null) {
            spec = spec.and((root, query, cb) -> {
                jakarta.persistence.criteria.Expression<Double> diff = cb
                        .diff(root.get("mrp").as(Double.class), root.get("sellingPrice").as(Double.class))
                        .as(Double.class);
                jakarta.persistence.criteria.Expression<Double> pct = cb
                        .prod(cb.quot(diff, root.get("mrp").as(Double.class)), 100.0).as(Double.class);
                return cb.greaterThanOrEqualTo(pct, minDiscount.doubleValue());
            });
        }

        if (dynamicFilters != null && !dynamicFilters.isEmpty()) {
            for (java.util.Map.Entry<String, String> entry : dynamicFilters.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                spec = spec.and((root, query, cb) -> {
                    // JSON path querying for Hibernate 6
                    // Wrap the key in double quotes to handle space-separated keys (ex: 'Device
                    // Type')
                    return cb.equal(
                            cb.function("JSON_UNQUOTE", String.class,
                                    cb.function("JSON_EXTRACT", String.class, root.get("attributes"),
                                            cb.literal("$.\"" + key + "\""))),
                            cb.literal(value));
                });
            }
        }

        return productRepository.findAll(spec, pageable)
                .map(this::convertToDTO);
    }

    public Page<ProductDTO> getProductsBySeller(Long sellerId, Pageable pageable) {
        log.info("Fetching products for sellerId={}", sellerId);

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        return productRepository.findBySeller(seller, pageable)
                .map(this::convertToDTO);
    }

    public ProductDTO toggleActive(Long id) {
        log.info("Toggling active status for product id={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));

        product.setIsActive(!product.getIsActive());
        Product saved = productRepository.save(product);

        return convertToDTO(saved);
    }

    public List<ProductDTO> getSimilarProducts(Long productId) {
        log.info("Fetching similar products for productId={}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));

        return productRepository.findByCategory(product.getCategory())
                .stream()
                .filter(p -> !p.getProductId().equals(productId) && p.getIsActive())
                .limit(5)
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<ProductDTO> getComparisonProducts(List<Long> productIds) {
        log.info("Fetching products for comparison: {}", productIds);
        return productRepository.findAllById(productIds)
                .stream()
                .filter(Product::getIsActive)
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<ProductVideo> getProductVideos(Long productId) {
        log.info("Fetching videos for productId={}", productId);
        return productVideoRepository.findByProductProductId(productId);
    }

    public ProductVideo addProductVideo(Long productId, String videoUrl, String videoType) {
        log.info("Adding video for productId={}, type={}", productId, videoType);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));

        ProductVideo video = new ProductVideo(product, videoUrl, videoType);
        return productVideoRepository.save(video);
    }

    private void mapDtoToEntity(ProductDTO dto, Product product) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setMrp(dto.getMrp());
        product.setSellingPrice(dto.getSellingPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setThresholdQuantity(dto.getThresholdQuantity());
        product.setIsActive(dto.getIsActive());
        product.setImageUrl(dto.getImageUrl());
        product.setAdditionalImages(dto.getAdditionalImages());
        if (dto.getAttributes() != null) {
            product.setAttributes(dto.getAttributes());
        }
    }

    private ProductDTO convertToDTO(Product product) {

        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setMrp(product.getMrp());
        dto.setSellingPrice(product.getSellingPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setThresholdQuantity(product.getThresholdQuantity());
        dto.setIsActive(product.getIsActive());
        dto.setCategoryId(product.getCategory().getCategoryId());
        dto.setSellerId(product.getSeller().getUserId());
        dto.setImageUrl(product.getImageUrl());
        dto.setAdditionalImages(product.getAdditionalImages());
        if (product.getAttributes() != null) {
            dto.setAttributes(product.getAttributes());
        }
        dto.setCategoryName(product.getCategory().getName());
        dto.setSellerName(product.getSeller().getUser() != null ? product.getSeller().getUser().getName() : "");

        return dto;
    }
}
