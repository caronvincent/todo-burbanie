# todo-web-app

This project is the technical challenge for CERN position BE-CSS-CSA-2024-216-GRAP.

Original author: Bartek Urbaniec

# Security

All routes require authentication and some require authorization. HTTP Basic auth is used.

This program does not include user management, it uses hard-coded users defined in `TodoApplication.java`.

Three users are provided:
- user (password `u1pass`), normal user
- userTwo (password `u2pass`), second normal user
- admin (password `admin`), admin user

# Endpoints

## Categories

This section describes endpoints related to categories. Categories are shared between all users.
Categories can be read by all users, but only administrators can create/update/delete categories.

### POST /categories

Creates a category. Needs `ADMIN` role.

Expects JSON with the following structure: `{"name" : "Name of category", "description" : "Category description"}`

Description is optional.

### GET /categories/{id}

Reads category `{id}`.

### PUT /categories/{id}

Updates a category. Needs `ADMIN` role.

Expects JSON with the following structure: `{"name" : "New name of category", "description" : "New category description"}`

Description is optional.

### DELETE /categories/{id}

Deletes category `{id}`. Needs `ADMIN` role.

If category `{id}` does not exist, nothing happens and status 200 is returned.

## Tasks

This section describes endpoints related to tasks. Tasks are user-private: users can CRUD their own tasks but not those of other users.
Administrators can CRUD any task.

### POST /tasks

Creates a task.

Expects JSON with the following structure: `{"name" : "Name of task", "description" : "Task description", "deadline" : "1970-01-01T00:00", "categoryId": 42}`

Description is optional. Deadline must be of format `YYYY-MM-DDTMM:SS`. Category must exist.

### GET /tasks/{id}

Reads task `{id}`. Task must belong to client or client must be `ADMIN`.

### PUT /tasks/{id}

Updates a task. Task must belong to client or client must be `ADMIN`.

Expects JSON with the following structure: `{"name" : "New name of task", "description" : "New task description", "deadline" : "2000-01-01T00:00", "categoryId": 1337}`

Description is optional. Deadline must be of format `YYYY-MM-DDTMM:SS`. Category must exist.

### DELETE /tasks/{id}

Deletes task `{id}`. Task must belong to client or client must be `ADMIN`.

If category `{id}` does not exist, nothing happens and status 200 is returned.

### GET /tasks/search

Searches tasks. Query string search parameters are available:
- `name`: tasks with name containing parameter
- `description`: tasks with description containing parameter
- `deadline`: tasks where deadline is parameter. Must be of format `YYYY-MM-DDTMM:SS`.
- `category`: tasks where category is parameter.
- `author`: tasks where author is parameter. Requires `ADMIN` role, returns status 403 otherwise.

If client is not `ADMIN`, search will only find tasks belonging to client.

# Testing

All routes have accompanying tests.

# CI

A GitHub Actions workflow has been defined to build and test the codebase on every push.

# Original README contents

This is a skeleton of Spring Boot application which should be used as a start point to create a working one.
The goal of this task is to create simple REST API  which allows users to manage TODOs. 
The API should allow to create/delete/update TODOs and categories as well as search for user, name, description, deadline and category in any combination. *For example find all todos for an user X where deadline is today and name contains test.* 
The API should also implement basic authorization/authentication: *User X cannot access TODOs of user Y as long as he doesn't have admin role.*

You are free to use any library or testing framework in the project.

Below you may find a proposition of the DB model:

![DB model](DBModel.png)

Once you are ready, please send me **link to your git repository** which contains complete solution
