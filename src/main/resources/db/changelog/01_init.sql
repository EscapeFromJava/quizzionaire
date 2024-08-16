CREATE TABLE questionnaire_item
(
    id       INTEGER PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    question VARCHAR(255),
    answer   VARCHAR(255),
    option1  VARCHAR(255),
    option2  VARCHAR(255),
    option3  VARCHAR(255),
    option4  VARCHAR(255),
    used     BOOLEAN NOT NULL DEFAULT FALSE,
    last     BOOLEAN NOT NULL DEFAULT FALSE
);

