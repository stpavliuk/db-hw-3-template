package org.example.app.item;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

        model.addAttribute("pageTitle", "Create Item");
        model.addAttribute("formAction", "/item/create");
        model.addAttribute("submitLabel", "Create item");
        return "item/form";
    }

    @PostMapping("/create")
    public String createItem(@RequestParam(required = false) String name,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) String price,
                             RedirectAttributes redirectAttributes) {
        var form = ItemForm.from(name, description, price);
        var validationError = form.validationError();
        if (validationError != null) {
            return redirectToFormError("create", redirectAttributes, validationError, form);
        }

        itemRepository.save(Item.of(form.name(), form.description(), form.priceValue()));
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

        model.addAttribute("pageTitle", "Edit Item");
        model.addAttribute("formAction", "/item/" + id + "/edit");
        model.addAttribute("submitLabel", "Save changes");
        return "item/form";
    }

    @PostMapping("/{id}/edit")
    public String updateItem(@PathVariable Long id,
                             @RequestParam(required = false) String name,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) String price,
                             RedirectAttributes redirectAttributes) {
        var item = itemRepository.findById(id).orElse(null);
        if (item == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Item not found.");
            return "redirect:/item";
        }

        var form = ItemForm.from(name, description, price);
        var validationError = form.validationError();
        if (validationError != null) {
            return redirectToFormError(id + "/edit", redirectAttributes, validationError, form);
        }

        itemRepository.save(item.withDetails(form.name(), form.description(), form.priceValue()));
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

    private String redirectToFormError(String pathSuffix,
                                       RedirectAttributes redirectAttributes,
                                       String message,
                                       ItemForm form) {
        redirectAttributes.addFlashAttribute("errorMessage", message);
        redirectAttributes.addFlashAttribute("itemForm", form);
        return "redirect:/item/" + pathSuffix;
    }

    public record ItemForm(String name, String description, String price) {
        public ItemForm {
            name = normalize(name);
            description = normalize(description);
            price = normalize(price);
        }

        public static ItemForm empty() {
            return new ItemForm("", "", "0.00");
        }

        public static ItemForm from(String name, String description, String price) {
            return new ItemForm(name, description, price);
        }

        public static ItemForm from(Item item) {
            return new ItemForm(item.name(), item.description(), item.price().toPlainString());
        }

        public String validationError() {
            if (!StringUtils.hasText(name)) {
                return "Name is required.";
            }
            if (!StringUtils.hasText(price)) {
                return "Price is required.";
            }

            try {
                var parsedPrice = new BigDecimal(price);
                if (parsedPrice.scale() > 2) {
                    return "Price must have at most 2 decimal places.";
                }
                if (parsedPrice.compareTo(BigDecimal.ZERO) < 0) {
                    return "Price must be non-negative.";
                }
            } catch (NumberFormatException exception) {
                return "Price must be a valid number.";
            }

            return null;
        }

        public BigDecimal priceValue() {
            return new BigDecimal(price).setScale(2);
        }

        private static String normalize(String value) {
            return value == null ? "" : value.trim();
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
