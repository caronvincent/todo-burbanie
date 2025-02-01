package ch.cern.todo;

import ch.cern.todo.model.Category;
import ch.cern.todo.model.Task;
import ch.cern.todo.repository.CategoryRepository;
import ch.cern.todo.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TaskTests extends TodoApplicationTests {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TaskRepository taskRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Category genericCategory;
    private final String genericUsername = "GenericUser";

    @BeforeEach
    public void setup() {
        genericCategory = categoryRepository.save(new Category("generic name", "generic description"));
    }

    @AfterEach
    public void tearDown() {
        taskRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Nested
    class CreationTests {
        @Test
        @WithMockUser(roles = "USER", username = genericUsername)
        void given_TaskData_when_PostTasks_then_TaskIsCreated() throws Exception {
            String name = "test name";
            String description = "test description";
            String deadline = "1970-01-01T00:00";

            Map<String, Object> inputOutput = new HashMap<>();
            inputOutput.put("name", name);
            inputOutput.put("description", description);
            inputOutput.put("deadline", deadline);
            inputOutput.put("categoryId", genericCategory.getId());
            String jsonAsString = objectMapper.writeValueAsString(inputOutput);

            mockMvc
                .perform(post("/tasks")
                    .content(jsonAsString)
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(content().json(jsonAsString));

            assertEquals(1, taskRepository.count());
            Iterable<Task> tasks = taskRepository.findAll();
            // Shouldn't loop
            for (Task task : tasks) {
                assertEquals(name, task.getName());
                assertEquals(description, task.getDescription());
                assertEquals(deadline, task.getDeadline().toString());
                assertEquals(genericUsername, task.getAuthor());
                assertEquals(genericCategory.getId(), task.getCategory().getId());
            }
        }
    }

    @Nested
    class ReadTests {
        @Test
        void given_ExistingTask_when_GetTasksId_then_TaskIsReturned() {
            throw new UnsupportedOperationException();
        }
    }

    @Nested
    class UpdateTests {
        @Test
        void given_ExistingTask_when_PutTasksId_then_TaskIsUpdated() {
            throw new UnsupportedOperationException();
        }
    }

    @Nested
    class DeleteTests {
        @Test
        void given_ExistingTask_when_DeleteTasksId_then_TaskIsDeleted() {
            throw new UnsupportedOperationException();
        }
    }

    @Nested
    class SearchTests {
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
}
