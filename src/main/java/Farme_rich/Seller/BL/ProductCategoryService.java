package Farme_rich.Seller.BL;


import Farme_rich.Seller.DTO.Request.CategoryRequestDTO;
import Farme_rich.Seller.DTO.Request.CategoryTreeDTO;
import Farme_rich.Seller.Model.BackEnd.ProductCategory;
import Farme_rich.Seller.Repo.BackEnd.ProductCategoryRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductCategoryService {

    @Autowired
    private ProductCategoryRepo repository;

    /**
     * Returns every category, nested as parent -> children.
     * Handles any depth, though the current data model is only 2 levels.
     */
    public List<CategoryTreeDTO> getCategoryTree() {
        List<ProductCategory> all = repository.findAll();

        Map<String, List<ProductCategory>> childrenByParentId = new HashMap<>();
        List<ProductCategory> parents = new ArrayList<>();

        for (ProductCategory c : all) {
            if (c.getParent_id() == null) {
                parents.add(c);
            } else {
                childrenByParentId
                        .computeIfAbsent(c.getParent_id().toHexString(), k -> new ArrayList<>())
                        .add(c);
            }
        }

        List<CategoryTreeDTO> tree = new ArrayList<>();
        for (ProductCategory parent : parents) {
            tree.add(buildNode(parent, childrenByParentId));
        }
        return tree;
    }

    private CategoryTreeDTO buildNode(ProductCategory cat, Map<String, List<ProductCategory>> childrenByParentId) {
        CategoryTreeDTO dto = toDTO(cat);
        List<ProductCategory> children = childrenByParentId.get(cat.get_id().toHexString());
        if (children != null) {
            for (ProductCategory child : children) {
                dto.getChildren().add(buildNode(child, childrenByParentId));
            }
        }
        return dto;
    }

    private CategoryTreeDTO toDTO(ProductCategory c) {
        CategoryTreeDTO dto = new CategoryTreeDTO();
        dto.setId(c.get_id().toHexString());
        dto.setCategory_name(c.getCategory_name());
        dto.setDescription(c.getDescription());
        dto.setCoverImagePath(c.getCoverImagePath());
        dto.setDisplayOnHomePage(c.isDisplayOnHomePage());
        dto.setParentId(c.getParent_id() != null ? c.getParent_id().toHexString() : null);

        System.out.println(
                "CATEGORY DTO: "
                        + c.getCategory_name()
                        + " | DB displayOnHomePage = "
                        + c.isDisplayOnHomePage()
                        + " | DTO displayOnHomePage = "
                        + dto.isDisplayOnHomePage()
        );
        return dto;
    }

    /**
     * Flat list — handy for populating the "Parent category" dropdown in the modal.
     */
    public List<CategoryTreeDTO> getAllFlat() {
        return repository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ProductCategory addCategory(CategoryRequestDTO req, MultipartFile file) {
        ProductCategory cat = new ProductCategory();

        applyRequestToEntity(cat, req);

        String now = Instant.now().toString();

        cat.setCreatedAt(now);
        cat.setUpdatedAt(now);

        if (file != null && !file.isEmpty()) {

            String companyName = req.getCompanyName();
            if (companyName == null || companyName.trim().isEmpty())
                throw new RuntimeException("Company name is required");

            String originalFilename = file.getOriginalFilename();

            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(
                        originalFilename.lastIndexOf(".")
                );
            }
            String fileName = originalFilename;

            Path uploadDirectory = Paths.get(
                    "uploads",
                    "CategoryCoverImages",
                    companyName
            );

            try {
                Files.createDirectories(uploadDirectory);

                Path filePath = uploadDirectory.resolve(fileName);

                Files.copy(
                        file.getInputStream(),
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );

            } catch (IOException e) {
                throw new RuntimeException(
                        "Failed to upload category image",
                        e
                );
            }

            cat.setCoverImagePath(fileName);
        }

        return repository.save(cat);
    }

    public ProductCategory updateCategory(String id, CategoryRequestDTO req, MultipartFile file) {

        ProductCategory cat = repository.findById(id).orElseThrow(() -> new RuntimeException("Category not found: " + id));

        applyRequestToEntity(cat, req);
        cat.setUpdatedAt(Instant.now().toString());

        if (file != null && !file.isEmpty()) {

            String companyName = req.getCompanyName();

            if (companyName == null || companyName.trim().isEmpty()) {
                throw new RuntimeException("Company name is required");
            }

            String originalFilename = file.getOriginalFilename();

            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(
                        originalFilename.lastIndexOf(".")
                );
            }

            String fileName = originalFilename;
            Path uploadDirectory = Paths.get("uploads", "CategoryCoverImages", companyName);
            try {
                Files.createDirectories(uploadDirectory);
                Path filePath = uploadDirectory.resolve(fileName);
                Files.copy(
                        file.getInputStream(),
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );

            } catch (IOException e) {

                throw new RuntimeException(
                        "Failed to upload category image",
                        e
                );
            }

            cat.setCoverImagePath(fileName);
        }

        return repository.save(cat);
    }

    private void applyRequestToEntity(ProductCategory cat, CategoryRequestDTO req) {
        cat.setCategory_name(req.getCategory_name());
        cat.setDescription(req.getDescription());
        cat.setCoverImagePath(req.getCoverImagePath());
        cat.setDisplayOnHomePage(req.isDisplayOnHomePage());

        String parentId = req.getParent_id();

        if (parentId != null) {
            parentId = parentId.trim();
        }
        if (parentId != null && !parentId.isEmpty() && !"null".equalsIgnoreCase(parentId) && ObjectId.isValid(parentId)) {
            cat.setParent_id(new ObjectId(parentId));
        } else {
            cat.setParent_id(null);
        }

        if (req.getSellerids() != null) {
            cat.setSellerids(req.getSellerids().stream().map(ObjectId::new).collect(Collectors.toList()));
        }
    }

    public void deleteCategory(String id) {
        // Optional safety: prevent deleting a parent that still has children.
        List<ProductCategory> children = repository.findByParentId(new ObjectId(id));
        if (!children.isEmpty()) {
            throw new RuntimeException("Cannot delete a category that still has sub-categories.");
        }
        repository.deleteById(id);
    }

}