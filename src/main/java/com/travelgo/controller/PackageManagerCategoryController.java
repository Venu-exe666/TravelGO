package com.travelgo.controller;

import com.travelgo.entity.PackageCategory;
import com.travelgo.service.PackageCategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for Package Category management in Admin panel.
 */
@Controller
@RequestMapping("/staff/categories")
public class PackageManagerCategoryController {

    private final PackageCategoryService categoryService;

    public PackageManagerCategoryController(PackageCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "staff/categories";
    }

    @PostMapping("/create")
    public String createCategory(@RequestParam("name") String name,
                                @RequestParam(value = "description", required = false) String description,
                                RedirectAttributes redirectAttributes) {
        try {
            PackageCategory cat = new PackageCategory();
            cat.setName(name);
            cat.setDescription(description);
            categoryService.save(cat);
            redirectAttributes.addFlashAttribute("successMessage", "Category created successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create category: " + e.getMessage());
        }
        return "redirect:/staff/categories";
    }

    @PostMapping("/{id}/edit")
    public String editCategory(@PathVariable("id") Long id,
                              @RequestParam("name") String name,
                              @RequestParam(value = "description", required = false) String description,
                              RedirectAttributes redirectAttributes) {
        try {
            PackageCategory cat = categoryService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            cat.setName(name);
            cat.setDescription(description);
            categoryService.save(cat);
            redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update category: " + e.getMessage());
        }
        return "redirect:/staff/categories";
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete category: " + e.getMessage());
        }
        return "redirect:/staff/categories";
    }
}
