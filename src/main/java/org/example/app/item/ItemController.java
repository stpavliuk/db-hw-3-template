package org.example.app.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;

@Controller
@RequestMapping("/item")
public class ItemController {

    private final ItemRepository itemRepository;

    public ItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @GetMapping
    public String listItems(Model model) {
        var items = new ArrayList<Item>();
        itemRepository.findAll().forEach(items::add);
        items.sort(Comparator.comparing(Item::id));

        model.addAttribute("items", items);
        model.addAttribute("priceSummary", PriceSummary.from(itemRepository.pricingSummary()));

        return "item/list";
    }

    @GetMapping("/create")
    public String createPage(Model model) {
        if (!model.containsAttribute("itemForm")) {
            model.addAttribute("itemForm", ItemForm.empty());
        }

        populateFormPage(model, "Create Item", "/item/create", "Create item");
        return "item/form";
    }

    @PostMapping("/create")
    public String createItem(@Valid @ModelAttribute("itemForm") ItemForm form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateFormPage(model, "Create Item", "/item/create", "Create item");
            return "item/form";
        }

        itemRepository.save(Item.of(form.nameValue(), form.descriptionValue(), form.priceValue()));
        redirectAttributes.addFlashAttribute("successMessage", "Item created.");
        return "redirect:/item";
    }

    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        var item = itemRepository.findById(id).orElse(null);
        if (item == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Item not found.");
            return "redirect:/item";
        }

        if (!model.containsAttribute("itemForm")) {
            model.addAttribute("itemForm", ItemForm.from(item));
        }

        populateFormPage(model, "Edit Item", "/item/" + id + "/edit", "Save changes");
        return "item/form";
    }

    @PostMapping("/{id}/edit")
    public String updateItem(@PathVariable Long id,
                             @Valid @ModelAttribute("itemForm") ItemForm form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        var item = itemRepository.findById(id).orElse(null);
        if (item == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Item not found.");
            return "redirect:/item";
        }

        if (bindingResult.hasErrors()) {
            populateFormPage(model, "Edit Item", "/item/" + id + "/edit", "Save changes");
            return "item/form";
        }

        itemRepository.save(item.withDetails(form.nameValue(), form.descriptionValue(), form.priceValue()));
        redirectAttributes.addFlashAttribute("successMessage", "Item updated.");
        return "redirect:/item";
    }

    @PostMapping("/{id}/delete")
    public String deleteItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (!itemRepository.existsById(id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Item not found.");
            return "redirect:/item";
        }

        itemRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Item deleted.");
        return "redirect:/item";
    }

    private void populateFormPage(Model model,
                                  String pageTitle,
                                  String formAction,
                                  String submitLabel) {
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("formAction", formAction);
        model.addAttribute("submitLabel", submitLabel);
    }

    public record ItemForm(
        @NotBlank(message = "Name is required.")
        @Size(max = 100, message = "Name must be at most 100 characters.")
        String name,
        @Size(max = 500, message = "Description must be at most 500 characters.")
        String description,
        @NotNull(message = "Price is required.")
        @DecimalMin(value = "0.00", message = "Price must be non-negative.")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 2 decimal places.")
        BigDecimal price
    ) {
        public static ItemForm empty() {
            return new ItemForm("", "", null);
        }

        public static ItemForm from(Item item) {
            return new ItemForm(item.name(), item.description(), item.price());
        }

        public BigDecimal priceValue() {
            return price.setScale(2, RoundingMode.HALF_UP);
        }

        public String nameValue() {
            return name.trim();
        }

        public String descriptionValue() {
            return description == null ? "" : description.trim();
        }
    }

    public record PriceSummary(BigDecimal average, BigDecimal max, BigDecimal min) {
        public static PriceSummary from(ItemRepository.PriceSummaryRow projection) {
            return new PriceSummary(
                normalize(projection.averagePrice()),
                normalize(projection.maxPrice()),
                normalize(projection.minPrice())
            );
        }

        private static BigDecimal normalize(BigDecimal value) {
            return value.setScale(2, RoundingMode.HALF_UP);
        }
    }
}
