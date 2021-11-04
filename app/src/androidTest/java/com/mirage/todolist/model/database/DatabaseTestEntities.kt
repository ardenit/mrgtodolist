package com.mirage.todolist.model.database

import java.util.*

val testEmailOne = "test@example.com"
val testEmailTwo = "prod@company.org"

val taskOne = TaskEntity(
    taskId = UUID.randomUUID(),
    accountName = testEmailOne,
    tasklistId = 1,
    taskIndex = 0
)
val taskTwo = TaskEntity(
    taskId = UUID.randomUUID(),
    accountName = testEmailOne,
    tasklistId = 1,
    taskIndex = 1
)
val testTasks = listOf(taskOne, taskTwo)

val tagOne = TagEntity(
    tagId = UUID.randomUUID(),
    accountName = testEmailOne,
    tagIndex = 0,
    name = "Tag0",
    styleIndex = 0,
    deleted = false
)
val tagTwo = TagEntity(
    tagId = UUID.randomUUID(),
    accountName = testEmailOne,
    tagIndex = 1,
    name = "Tag1",
    styleIndex = 1,
    deleted = false
)
val deletedTag = TagEntity(
    tagId = UUID.randomUUID(),
    accountName = testEmailOne,
    tagIndex = 1,
    name = "Tag2",
    styleIndex = 2,
    deleted = true
)
val testTags = listOf(tagOne, tagTwo, deletedTag)

val relationOne = RelationEntity(
    taskId = taskOne.taskId,
    tagId = tagOne.tagId,
    accountName = taskOne.accountName,
    deleted = false
)
val relationTwo = RelationEntity(
    taskId = taskTwo.taskId,
    tagId = tagOne.tagId,
    accountName = taskTwo.accountName,
    deleted = false
)
val testRelations = listOf(relationOne, relationTwo)

val versionOne = VersionEntity(
    accountName = testEmailOne,
    dataVersion = UUID.randomUUID(),
    mustBeProcessed = false
)
val versionTwo = VersionEntity(
    accountName = testEmailTwo,
    dataVersion = UUID.randomUUID(),
    mustBeProcessed = false
)
val testVersions = listOf(versionOne, versionTwo)