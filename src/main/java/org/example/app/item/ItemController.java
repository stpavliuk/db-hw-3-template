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
        items.sort(Comparator.comparing(Item::getId));
        model.addAttribute("items", items);
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
                             RedirectAttributes redirectAttributes) {
        var form = ItemForm.from(name, description);
        if (!form.isValid()) {
            return redirectToFormError("create", redirectAttributes, "Name is required.", form);
        }

        itemRepository.save(new Item(null, form.getName(), form.getDescription()));
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
            model.addAttribute("itemForm", ItemForm.from(item.getName(), item.getDescription()));
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
                             RedirectAttributes redirectAttributes) {
        var item = itemRepository.findById(id).orElse(null);
        if (item == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Item not found.");
            return "redirect:/item";
        }

        var form = ItemForm.from(name, description);
        if (!form.isValid()) {
            return redirectToFormError(id + "/edit", redirectAttributes, "Name is required.", form);
        }

        item.setName(form.getName());
        item.setDescription(form.getDescription());
        itemRepository.save(item);
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

    public static final class ItemForm {
        private final String name;
        private final String description;

        private ItemForm(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public static ItemForm empty() {
            return new ItemForm("", "");
        }

        public static ItemForm from(String name, String description) {
            return new ItemForm(normalize(name), normalize(description));
        }

        public boolean isValid() {
            return StringUtils.hasText(name);
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        private static String normalize(String value) {
            return value == null ? "" : value.trim();
        }
    }
}
