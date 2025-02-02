package ch.cern.todo;

import ch.cern.todo.model.Category;
import ch.cern.todo.model.NewTaskDto;
import ch.cern.todo.model.PersistedTaskDto;
import ch.cern.todo.model.Task;
import ch.cern.todo.repository.CategoryRepository;
import ch.cern.todo.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        void given_TaskData_when_PostTask_then_TaskIsCreated() throws Exception {
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

        @Test
        void given_AnonymousClient_when_PostTask_then_Status401() throws Exception {
            String name = "doesn't matter";
            String description = "doesn't matter";
            String deadline = "1970-01-01T00:00";

            Map<String, Object> body = new HashMap<>();
            body.put("name", name);
            body.put("description", description);
            body.put("deadline", deadline);
            body.put("categoryId", genericCategory.getId());

            mockMvc
                .perform(post("/tasks")
                    .content(objectMapper.writeValueAsString(objectMapper.writeValueAsString(body)))
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER", username = genericUsername)
        void given_MissingDescription_when_PostTask_then_Status201() throws Exception {
            String name = "missing description";
            String deadline = "1970-01-01T00:00";

            Map<String, Object> inputOutput = new HashMap<>();
            inputOutput.put("name", name);
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
                assertNull(task.getDescription());
                assertEquals(deadline, task.getDeadline().toString());
                assertEquals(genericUsername, task.getAuthor());
                assertEquals(genericCategory.getId(), task.getCategory().getId());
            }
        }

        @Test
        @WithMockUser(roles = "USER", username = genericUsername)
        void given_MissingRequiredData_when_PostTask_then_Status400() throws Exception {
            String description = "missing name deadline and category";

            Map<String, Object> inputOutput = new HashMap<>();
            inputOutput.put("name", description);
            String jsonAsString = objectMapper.writeValueAsString(inputOutput);

            mockMvc
                .perform(post("/tasks")
                    .content(jsonAsString)
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class ReadTests {
        private Task preExistingTask;

        @BeforeEach
        void setUp() {
            preExistingTask = taskRepository.save(
                new Task(
                    new NewTaskDto(
                        "test title",
                        "test description",
                        "1970-01-01T00:00",
                        genericCategory.getId()
                    ),
                    genericCategory,
                    genericUsername
                )
            );
        }

        @Test
        @WithMockUser(roles = "USER", username = genericUsername)
        void given_ExistingTask_when_GetTask_then_TaskIsReturned() throws Exception {
            Map<String, Object> expected = new HashMap<>();
            expected.put("name", preExistingTask.getName());
            expected.put("description", preExistingTask.getDescription());
            expected.put("deadline", preExistingTask.getDeadline().toString());
            expected.put("categoryId", genericCategory.getId());

            mockMvc
                .perform(get("/tasks/" + preExistingTask.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));
        }

        @Test
        @WithMockUser(roles = "USER", username = genericUsername)
        void given_MissingTask_when_GetTask_then_Status404() throws Exception {
            mockMvc
                .perform(get("/tasks/999999999"))
                .andExpect(status().isNotFound());
        }

        @Test
        void given_AnonymousClient_when_GetTask_then_Status401() throws Exception {
            mockMvc
                .perform(get("/tasks/1"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER", username = "DifferentUser")
        void given_ExistingTask1FromUser1_when_GetTask1FromUser2_then_Status403() throws Exception {
            mockMvc
                .perform(get("/tasks/" + preExistingTask.getId()))
                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN", username = "Administrator")
        void given_ExistingTaskFromUser_when_GetTaskFromAdmin_then_TaskIsReturned() throws Exception {
            Task newTask = taskRepository.save(
                new Task(
                    new NewTaskDto(
                        "test title",
                        "test description",
                        "1970-01-01T00:00",
                        genericCategory.getId()
                    ),
                    genericCategory,
                    genericUsername
                )
            );

            mockMvc
                .perform(get("/tasks/" + newTask.getId()))
                .andExpect(status().isOk());
        }
    }

    @Nested
    class UpdateTests {
        private Task preExistingTask;

        @BeforeEach
        void setUp() {
            preExistingTask = taskRepository.save(
                new Task(
                    new NewTaskDto(
                        "test title",
                        "test description",
                        "1970-01-01T00:00",
                        genericCategory.getId()
                    ),
                    genericCategory,
                    genericUsername
                )
            );
        }

        @Test
        @WithMockUser(roles = "USER", username = genericUsername)
        void given_ExistingTask_when_PutTask_then_TaskIsUpdated() throws Exception {
            String name = "new name";
            String description = "new description";
            String deadline = "2000-01-01T00:00";
            Category secondCategory = categoryRepository.save(new Category("other", "other"));

            Map<String, Object> inputOutput = new HashMap<>();
            inputOutput.put("name", name);
            inputOutput.put("description", description);
            inputOutput.put("deadline", deadline);
            inputOutput.put("categoryId", secondCategory.getId());
            String inputOutputAsString = objectMapper.writeValueAsString(inputOutput);

            mockMvc
                .perform(put("/tasks/" + preExistingTask.getId())
                    .content(inputOutputAsString)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(inputOutputAsString));

            assertEquals(1, taskRepository.count());
            Task updatedTask = taskRepository.findById(preExistingTask.getId()).orElseThrow();
            assertEquals(name, updatedTask.getName());
            assertEquals(description, updatedTask.getDescription());
            assertEquals(deadline, updatedTask.getDeadline().toString());
            assertEquals(genericUsername, updatedTask.getAuthor());
            assertEquals(secondCategory.getId(), updatedTask.getCategory().getId());
        }

        @Test
        @WithMockUser(roles = "USER", username = genericUsername)
        void given_MissingTask_when_PutTask_then_Status404() throws Exception {
            Map<String, Object> inputOutput = new HashMap<>();
            inputOutput.put("name", "doesn't matter");
            inputOutput.put("description", "doesn't matter");
            inputOutput.put("deadline", "1970-01-01T00:00");
            inputOutput.put("categoryId", genericCategory.getId());

            mockMvc
                .perform(put("/tasks/999999999")
                    .content(objectMapper.writeValueAsString(inputOutput))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USER", username = genericUsername)
        void given_MissingDescription_when_PutTask_then_TaskIsUpdated() throws Exception {
            String name = "new name";
            String deadline = "2000-01-01T00:00";
            Category secondCategory = categoryRepository.save(new Category("other", "other"));

            Map<String, Object> inputOutput = new HashMap<>();
            inputOutput.put("name", name);
            inputOutput.put("deadline", deadline);
            inputOutput.put("categoryId", secondCategory.getId());
            String inputOutputAsString = objectMapper.writeValueAsString(inputOutput);

            mockMvc
                .perform(put("/tasks/" + preExistingTask.getId())
                    .content(inputOutputAsString)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(inputOutputAsString));

            assertEquals(1, taskRepository.count());
            Task updatedTask = taskRepository.findById(preExistingTask.getId()).orElseThrow();
            assertEquals(name, updatedTask.getName());
            assertNull(updatedTask.getDescription());
            assertEquals(deadline, updatedTask.getDeadline().toString());
            assertEquals(genericUsername, updatedTask.getAuthor());
            assertEquals(secondCategory.getId(), updatedTask.getCategory().getId());
        }

        @Test
        @WithMockUser(roles = "USER", username = genericUsername)
        void given_MissingRequiredData_when_PutTask_then_Status400() throws Exception {
            Map<String, Object> inputOutput = new HashMap<>();
            inputOutput.put("description", "missing name, deadline and category");
            String inputOutputAsString = objectMapper.writeValueAsString(inputOutput);

            mockMvc
                .perform(put("/tasks/" + preExistingTask.getId())
                    .content(inputOutputAsString)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        }

        @Test
        void given_AnonymousClient_when_PutTask_then_Status401() throws Exception {
            mockMvc
                .perform(put("/tasks/" + preExistingTask.getId()))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER", username = "DifferentUser")
        void given_ExistingTask1FromUser1_when_PutTask1FromUser2_then_Status403() throws Exception {
            String name = "new name";
            String description = "new description";
            String deadline = "2000-01-01T00:00";
            Category secondCategory = categoryRepository.save(new Category("other", "other"));

            Map<String, Object> inputOutput = new HashMap<>();
            inputOutput.put("name", name);
            inputOutput.put("description", description);
            inputOutput.put("deadline", deadline);
            inputOutput.put("categoryId", secondCategory.getId());
            String inputOutputAsString = objectMapper.writeValueAsString(inputOutput);

            mockMvc
                .perform(put("/tasks/" + preExistingTask.getId())
                    .content(inputOutputAsString)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN", username = "Administrator")
        void given_ExistingTaskFromUser_when_PutTaskFromAdmin_then_TaskIsUpdated() throws Exception {
            String name = "new name";
            String description = "new description";
            String deadline = "2000-01-01T00:00";
            Category secondCategory = categoryRepository.save(new Category("other", "other"));

            Map<String, Object> inputOutput = new HashMap<>();
            inputOutput.put("name", name);
            inputOutput.put("description", description);
            inputOutput.put("deadline", deadline);
            inputOutput.put("categoryId", secondCategory.getId());
            String inputOutputAsString = objectMapper.writeValueAsString(inputOutput);

            mockMvc
                .perform(put("/tasks/" + preExistingTask.getId())
                    .content(inputOutputAsString)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(inputOutputAsString));

            assertEquals(1, taskRepository.count());
            Task updatedTask = taskRepository.findById(preExistingTask.getId()).orElseThrow();
            assertEquals(name, updatedTask.getName());
            assertEquals(description, updatedTask.getDescription());
            assertEquals(deadline, updatedTask.getDeadline().toString());
            assertEquals(genericUsername, updatedTask.getAuthor());
            assertEquals(secondCategory.getId(), updatedTask.getCategory().getId());
        }
    }

    @Nested
    class DeleteTests {
        private Task preExistingTask;

        @BeforeEach
        void setUp() {
            preExistingTask = taskRepository.save(
                new Task(
                    new NewTaskDto(
                        "test title",
                        "test description",
                        "1970-01-01T00:00",
                        genericCategory.getId()
                    ),
                    genericCategory,
                    genericUsername
                )
            );
        }

        @Test
        @WithMockUser(roles = "USER", username = genericUsername)
        void given_ExistingTask_when_DeleteTask_then_TaskIsDeleted() throws Exception {
            mockMvc
                .perform(delete("/tasks/" + preExistingTask.getId()))
                .andExpect(status().isOk());
            assertEquals(0, taskRepository.count());
        }

        @Test
        @WithMockUser(roles = "USER", username = "DifferentUser")
        void given_ExistingTask1FromUser1_when_DeleteTask1FromUser2_then_Status403() throws Exception {
            mockMvc
                .perform(delete("/tasks/" + preExistingTask.getId()))
                .andExpect(status().isForbidden());
            assertEquals(1, taskRepository.count());
        }

        @Test
        @WithMockUser(roles = "ADMIN", username = "Administrator")
        void given_ExistingTaskFromUser_when_DeleteTaskFromAdmin_then_TaskIsDeleted() throws Exception {
            mockMvc
                .perform(delete("/tasks/" + preExistingTask.getId()))
                .andExpect(status().isOk());
            assertEquals(0, taskRepository.count());
        }

        @Test
        void given_AnonymousClient_when_DeleteTask_then_Status401() throws Exception {
            mockMvc
                .perform(delete("/tasks/" + preExistingTask.getId()))
                .andExpect(status().isUnauthorized());
            assertEquals(1, taskRepository.count());
        }

        @Test
        @WithMockUser(roles = "USER", username = genericUsername)
        void given_MissingTask_when_DeleteTask_then_NothingHappensStatus200() throws Exception {
            mockMvc
                .perform(delete("/tasks/999999999"))
                .andExpect(status().isOk());
            assertEquals(1, taskRepository.count());
        }
    }

    @Nested
    class SearchTests {
        @Test
        @WithMockUser(roles = "ADMIN", username = "Administrator")
        void given_ExistingTasks_when_SearchTasksByAuthorAsAdmin_then_TasksAreReturned() throws Exception {
            List<PersistedTaskDto> taskList = new ArrayList<>();

            taskRepository.save(
                new Task(
                    new NewTaskDto(
                        "test title",
                        "test description",
                        "1970-01-01T00:00",
                        genericCategory.getId()
                    ),
                    genericCategory,
                    "OtherUserName"
                )
            );

            taskList.add(new PersistedTaskDto(taskRepository.save(
                new Task(
                    new NewTaskDto(
                        "test title",
                        "test description",
                        "1970-01-01T00:00",
                        genericCategory.getId()
                    ),
                    genericCategory,
                    genericUsername
                )
            )));

            mockMvc
                .perform(get("/tasks/search?author=" + genericUsername))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(taskList)));
        }

        @Test
        @WithMockUser(roles = "USER", username = genericUsername)
        void given_ExistingTasks_when_SearchTasksByAuthorAsUser_then_Status403() throws Exception {
            mockMvc
                .perform(get("/tasks/search?author=" + genericUsername))
                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "USER", username = genericUsername)
        void given_ExistingTasks_when_SearchTasksByName_then_TasksAreReturned() throws Exception {
            String nameToSearch = "name to search";

            taskRepository.save(
                new Task(
                    new NewTaskDto(
                        "name to ignore",
                        "test description",
                        "1970-01-01T00:00",
                        genericCategory.getId()
                    ),
                    genericCategory,
                    genericUsername
                )
            );
            List<PersistedTaskDto> taskList = new ArrayList<>();
            taskList.add(new PersistedTaskDto(taskRepository.save(
                new Task(
                    new NewTaskDto(
                        nameToSearch,
                        "test description",
                        "1970-01-01T00:00",
                        genericCategory.getId()
                    ),
                    genericCategory,
                    genericUsername
                )
            )));

            mockMvc
                .perform(get("/tasks/search?name=" + nameToSearch))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(taskList)));
        }

        @Test
        @WithMockUser(roles = "USER", username = genericUsername)
        void given_ExistingTasks_when_SearchTasksByDescription_then_TasksAreReturned() throws Exception {
            String descriptionToSearch = "description to search";

            taskRepository.save(
                new Task(
                    new NewTaskDto(
                        "test title",
                        "description to ignore",
                        "1970-01-01T00:00",
                        genericCategory.getId()
                    ),
                    genericCategory,
                    genericUsername
                )
            );
            List<PersistedTaskDto> taskList = new ArrayList<>();
            taskList.add(new PersistedTaskDto(taskRepository.save(
                new Task(
                    new NewTaskDto(
                        "test title",
                        descriptionToSearch,
                        "1970-01-01T00:00",
                        genericCategory.getId()
                    ),
                    genericCategory,
                    genericUsername
                )
            )));

            mockMvc
                .perform(get("/tasks/search?description=" + descriptionToSearch))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(taskList)));
        }

        @Test
        @WithMockUser(roles = "USER", username = genericUsername)
        void given_ExistingTasks_when_SearchTasksByDeadline_then_TasksAreReturned() throws Exception {
            String deadlineToSearch = "1970-01-01T00:00";

            taskRepository.save(
                new Task(
                    new NewTaskDto(
                        "test title",
                        "test description",
                        "2000-01-01T00:00",
                        genericCategory.getId()
                    ),
                    genericCategory,
                    genericUsername
                )
            );

            List<PersistedTaskDto> taskList = new ArrayList<>();
            taskList.add(new PersistedTaskDto(taskRepository.save(
                new Task(
                    new NewTaskDto(
                        "test title",
                        "test description",
                        deadlineToSearch,
                        genericCategory.getId()
                    ),
                    genericCategory,
                    genericUsername
                )
            )));

            String jsonAsString = objectMapper.writeValueAsString(taskList);

            mockMvc
                .perform(get("/tasks/search?deadline=" + deadlineToSearch))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAsString));
        }

        @Test
        @WithMockUser(roles = "USER", username = genericUsername)
        void given_ExistingTasks_when_SearchTasksByCategory_then_TasksAreReturned() throws Exception {
            Category categoryToIgnore = categoryRepository.save(new Category("other name", "other description"));
            taskRepository.save(
                new Task(
                    new NewTaskDto(
                        "test title",
                        "test description",
                        "1970-01-01T00:00",
                        categoryToIgnore.getId()
                    ),
                    categoryToIgnore,
                    genericUsername
                )
            );

            List<PersistedTaskDto> taskList = new ArrayList<>();
            taskList.add(new PersistedTaskDto(taskRepository.save(
                new Task(
                    new NewTaskDto(
                        "test title",
                        "test description",
                        "1970-01-01T00:00",
                        genericCategory.getId()
                    ),
                    genericCategory,
                    genericUsername
                )
            )));

            mockMvc
                .perform(get("/tasks/search?category=" + genericCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(taskList)));
        }

        @Test
        @WithMockUser(roles = "USER", username = genericUsername)
        void given_ExistingTasks_when_SearchTasksByMultipleCriterion_then_TasksAreReturned() throws Exception {
            String targetTitle = "target title";
            String targetDescription = "target description";

            taskRepository.save(
                new Task(
                    new NewTaskDto(
                        "not matching title",
                        targetDescription,
                        "1970-01-01T00:00",
                        genericCategory.getId()
                    ),
                    genericCategory,
                    genericUsername
                )
            );
            taskRepository.save(
                new Task(
                    new NewTaskDto(
                        targetTitle,
                        "not matching description",
                        "1970-01-01T00:00",
                        genericCategory.getId()
                    ),
                    genericCategory,
                    genericUsername
                )
            );

            List<PersistedTaskDto> taskList = new ArrayList<>();
            taskList.add(new PersistedTaskDto(taskRepository.save(
                new Task(
                    new NewTaskDto(
                        targetTitle,
                        targetDescription,
                        "1970-01-01T00:00",
                        genericCategory.getId()
                    ),
                    genericCategory,
                    genericUsername
                )
            )));

            mockMvc
                .perform(get("/tasks/search?name=" + targetTitle + "&description=" + targetDescription))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(taskList)));
        }

        @Test
        @WithMockUser(roles = "USER", username = genericUsername)
        void given_ExistingTasks_when_SearchTasksByNoCriteria_then_AllTasksAreReturned() throws Exception {
            List<PersistedTaskDto> taskList = new ArrayList<>();
            taskList.add(new PersistedTaskDto(taskRepository.save(
                new Task(
                    new NewTaskDto(
                        "test name",
                        "test description",
                        "1970-01-01T00:00",
                        genericCategory.getId()
                    ),
                    genericCategory,
                    genericUsername
                )
            )));

            mockMvc
                .perform(get("/tasks/search"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(taskList)));
        }
    }
}
