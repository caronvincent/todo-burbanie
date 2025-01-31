package ch.cern.todo;

import ch.cern.todo.model.Category;
import ch.cern.todo.model.NewCategoryDto;
import ch.cern.todo.repository.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TodoApplicationTests {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private CategoryRepository categoryRepository;

	// Category tests

	@Test
	@WithMockUser(roles = "ADMIN")
	void given_CategoryData_when_PostCategories_then_CategoryIsCreated() throws Exception {
		Map<String, Object> input = new HashMap<>();
		input.put("name", "test title");
		input.put("description", "test description");

		Map<String, Object> expected = new HashMap<>();
		expected.put("id", 1);
		expected.put("name", "test title");
		expected.put("description", "test description");

		ObjectMapper objectMapper = new ObjectMapper();

		this.mockMvc
			.perform(post("/categories")
					.content(objectMapper.writeValueAsString(input))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(content().json(objectMapper.writeValueAsString(expected)));
	}

	@Test
	@WithMockUser(roles = "USER")
	void given_ExistingCategory_when_GetCategoriesId_then_CategoryIsReturned() throws Exception {
		categoryRepository.save(new Category(new NewCategoryDto("test title", "test description")));

		Map<String, Object> expected = new HashMap<>();
		expected.put("id", 1);
		expected.put("name", "test title");
		expected.put("description", "test description");

		ObjectMapper objectMapper = new ObjectMapper();

		this.mockMvc
			.perform(get("/categories/1"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected)));
	}

	@Test
	void given_ExistingCategory_when_PutCategoriesId_then_CategoryIsUpdated() {
		throw new UnsupportedOperationException();
	}

	@Test
	void given_ExistingCategory_when_DeleteCategoriesId_then_CategoryIsDeleted() {
		throw new UnsupportedOperationException();
	}

	@Test
	void given_UserIsNotAdmin_when_AnyCategoryOperation_then_Status403() {
		throw new UnsupportedOperationException();
	}

	// Task tests

	@Test
	void given_TaskData_when_PostTasks_then_TaskIsCreated() {
		throw new UnsupportedOperationException();
	}

	@Test
	void given_ExistingTask_when_GetTasksId_then_TaskIsReturned() {
		throw new UnsupportedOperationException();
	}

	@Test
	void given_ExistingTask_when_PutTasksId_then_TaskIsUpdated() {
		throw new UnsupportedOperationException();
	}

	@Test
	void given_ExistingTask_when_DeleteTasksId_then_TaskIsDeleted() {
		throw new UnsupportedOperationException();
	}

	@Test
	void given_ExistingTasks_when_SearchTasksByAuthor_then_TasksAreReturned() {
		throw new UnsupportedOperationException();
	}

	@Test
	void given_ExistingTasks_when_SearchTasksByName_then_TasksAreReturned() {
		throw new UnsupportedOperationException();
	}

	@Test
	void given_ExistingTasks_when_SearchTasksByDescription_then_TasksAreReturned() {
		throw new UnsupportedOperationException();
	}

	@Test
	void given_ExistingTasks_when_SearchTasksByDeadline_then_TasksAreReturned() {
		throw new UnsupportedOperationException();
	}

	@Test
	void given_ExistingTasks_when_SearchTasksByCategory_then_TasksAreReturned() {
		throw new UnsupportedOperationException();
	}

	@Test
	void given_ExistingTasks_when_SearchTasksByMultipleCriterion_then_TasksAreReturned() {
		throw new UnsupportedOperationException();
	}

	@Test
	void given_ExistingTaskFromUser1_when_InteractionWithTaskFromUser2_then_Status403() {
		throw new UnsupportedOperationException();
	}

	@Test
	void given_ExistingTaskFromUser1_when_InteractionWithTaskFromAdmin_then_Success() {
		throw new UnsupportedOperationException();
	}
}
