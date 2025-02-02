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

This section describes endpoints related to tasks. Tasks are user-private: users can CRUD their own tasks but not those of other users. Administrators can CRUD any task.

### POST /tasks

Creates a task.

Expects JSON with the following structure: `{"name" : "Name of task", "description" : "Task description", "deadline" : "1970-01-01T00:00", "categoryId": 42}`

Description is optional. Deadline must be of format `YYYY-MM-DDTMM:SS`. Category must exist.

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
