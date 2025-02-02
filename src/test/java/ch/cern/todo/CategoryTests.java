package ch.cern.todo;

import ch.cern.todo.model.Category;
import ch.cern.todo.model.NewCategoryDto;
import ch.cern.todo.repository.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CategoryTests extends TodoApplicationTests {
    @Autowired
    private CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    public void tearDown() {
        categoryRepository.deleteAll();
    }

    @Nested
    class CreationTests {
        @Test
        @WithMockUser(roles = "ADMIN")
        void given_CategoryData_when_PostCategories_then_Status201() throws Exception {
            String title = "test title";
            String description = "test description";

            Map<String, Object> inputOutput = new HashMap<>();
            inputOutput.put("name", title);
            inputOutput.put("description", description);

            mockMvc
                .perform(post("/categories")
                    .content(objectMapper.writeValueAsString(inputOutput))
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(inputOutput)));

            assertEquals(1, categoryRepository.count());
            Iterable<Category> categories = categoryRepository.findAll();
            // Shouldn't loop
            for (Category category : categories) {
                assertEquals(title, category.getName());
                assertEquals(description, category.getDescription());
            }
        }

        @Test
        @WithMockUser(roles = "USER")
        void given_UserNotAdmin_when_PostCategories_then_Status403() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("name", "doesn't matter");
            input.put("description", "doesn't matter");

            mockMvc
                .perform(post("/categories")
                    .content(objectMapper.writeValueAsString(input))
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
        }

        @Test
        void given_AnonymousClient_when_PostCategories_then_Status401() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("name", "doesn't matter");
            input.put("description", "doesn't matter");

            mockMvc
                .perform(post("/categories")
                    .content(objectMapper.writeValueAsString(input))
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void given_MissingName_when_PostCategories_then_Status400() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("description", "missing name");

            mockMvc
                .perform(post("/categories")
                    .content(objectMapper.writeValueAsString(input))
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void given_MissingDescription_when_PostCategories_then_Status201() throws Exception {
            String title = "missing optional description";

            Map<String, Object> inputOutput = new HashMap<>();
            inputOutput.put("name", title);
            String inputOutputAsString = objectMapper.writeValueAsString(inputOutput);

            mockMvc
                .perform(post("/categories")
                    .content(inputOutputAsString)
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(content().json(inputOutputAsString));

            assertEquals(1, categoryRepository.count());
            Iterable<Category> categories = categoryRepository.findAll();
            // Shouldn't loop
            for (Category category : categories) {
                assertEquals(title, category.getName());
                assertNull(category.getDescription());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void given_TitleTooLong_when_PostCategories_then_Status400() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("name", "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
            input.put("description", "doesn't matter");

            mockMvc
                .perform(post("/categories")
                    .content(objectMapper.writeValueAsString(input))
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class ReadTests {
        @Test
        @WithMockUser(roles = "USER")
        void given_ExistingCategory_when_GetCategory_then_CategoryIsReturned() throws Exception {
            String name = "test title";
            String description = "test description";

            Category newCategory = categoryRepository.save(new Category(new NewCategoryDto(name, description)));

            Map<String, Object> expected = new HashMap<>();
            expected.put("name", name);
            expected.put("description", description);

            mockMvc
                .perform(get("/categories/" + newCategory.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));
        }

        @Test
        @WithMockUser(roles = "USER")
        void given_MissingCategory_when_GetCategory_then_Status404() throws Exception {
            mockMvc
                .perform(get("/categories/999999999"))
                .andExpect(status().isNotFound());
        }

        @Test
        void given_AnonymousClient_when_GetCategory_then_Status401() throws Exception {
            mockMvc
                .perform(get("/categories/1"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class UpdateTests {
        @Test
        @WithMockUser(roles = "ADMIN")
        void given_ExistingCategory_when_PutCategory_then_CategoryIsUpdated() throws Exception {
            Category newCategory = categoryRepository.save(new Category(new NewCategoryDto("first title", "first description")));

            String name = "new title";
            String description = "new description";

            Map<String, Object> inputOutput = new HashMap<>();
            inputOutput.put("name", name);
            inputOutput.put("description", description);

            mockMvc
                .perform(put("/categories/" + newCategory.getId())
                    .content(objectMapper.writeValueAsString(inputOutput))
                    .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(inputOutput)));

            assertEquals(1, categoryRepository.count());
            Category updatedCategory = categoryRepository.findById(newCategory.getId()).orElseThrow();
            assertEquals(name, updatedCategory.getName());
            assertEquals(description, updatedCategory.getDescription());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void given_MissingCategory_when_PutCategory_then_Status404() throws Exception {
            Map<String, Object> body = new HashMap<>();
            body.put("name", "doesn't matter");
            body.put("description", "doesn't matter");

            mockMvc
                .perform(put("/categories/999999999")
                    .content(objectMapper.writeValueAsString(body))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USER")
        void given_UserNotAdmin_when_PutCategory_then_Status403() throws Exception {
            Category newCategory = categoryRepository.save(new Category(new NewCategoryDto("doesn't matter", "doesn't matter")));

            Map<String, Object> body = new HashMap<>();
            body.put("name", "doesn't matter");
            body.put("description", "doesn't matter");

            mockMvc
                .perform(put("/categories/" + newCategory.getId())
                    .content(objectMapper.writeValueAsString(body))
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
        }

        @Test
        void given_AnonymousClient_when_PutCategory_then_Status401() throws Exception {
            Category newCategory = categoryRepository.save(new Category(new NewCategoryDto("doesn't matter", "doesn't matter")));

            Map<String, Object> body = new HashMap<>();
            body.put("name", "doesn't matter");
            body.put("description", "doesn't matter");

            mockMvc
                .perform(post("/categories/" + newCategory.getId())
                    .content(objectMapper.writeValueAsString(body))
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void given_MissingName_when_PutCategory_then_Status400() throws Exception {
            Category newCategory = categoryRepository.save(new Category(new NewCategoryDto("doesn't matter", "doesn't matter")));

            Map<String, Object> input = new HashMap<>();
            input.put("description", "missing name");

            mockMvc
                .perform(put("/categories/" + newCategory.getId())
                    .content(objectMapper.writeValueAsString(input))
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void given_MissingDescription_when_PutCategory_then_CategoryIsUpdated() throws Exception {
            Category newCategory = categoryRepository.save(new Category(new NewCategoryDto("first title", "first description")));

            String name = "new title";
            Map<String, Object> inputOutput = new HashMap<>();
            inputOutput.put("name", name);
            String inputOutputAsString = objectMapper.writeValueAsString(inputOutput);

            mockMvc
                .perform(put("/categories/" + newCategory.getId())
                    .content(inputOutputAsString)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(inputOutputAsString));

            assertEquals(1, categoryRepository.count());
            Category updatedCategory = categoryRepository.findById(newCategory.getId()).orElseThrow();
            assertEquals(name, updatedCategory.getName());
            assertNull(updatedCategory.getDescription());
        }

    }

    @Nested
    class DeleteTests {
        @Test
        @WithMockUser(roles = "ADMIN")
        void given_ExistingCategory_when_DeleteCategory_then_CategoryIsDeleted() throws Exception {
            Category newCategory = categoryRepository.save(new Category(new NewCategoryDto("doesn't matter", "doesn't matter")));
            mockMvc
                .perform(delete("/categories/" + newCategory.getId()))
                .andExpect(status().isOk());
            assertEquals(0, categoryRepository.count());
        }

        @Test
        @WithMockUser(roles = "USER")
        void given_UserNotAdmin_when_DeleteCategory_then_Status403() throws Exception {
            Category newCategory = categoryRepository.save(new Category(new NewCategoryDto("doesn't matter", "doesn't matter")));
            mockMvc
                .perform(delete("/categories/" + newCategory.getId()))
                .andExpect(status().isForbidden());
        }

        @Test
        void given_AnonymousClient_when_DeleteCategory_then_Status401() throws Exception {
            Category newCategory = categoryRepository.save(new Category(new NewCategoryDto("doesn't matter", "doesn't matter")));
            mockMvc
                .perform(delete("/categories/" + newCategory.getId()))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void given_MissingCategory_when_DeleteCategory_then_NothingHappensStatus200() throws Exception {
            mockMvc
                .perform(delete("/categories/999999999"))
                .andExpect(status().isOk());
        }
    }
}
