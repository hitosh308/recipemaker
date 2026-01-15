CREATE TABLE recipe (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    servings INT,
    cook_time_minutes INT,
    steps_json CLOB,
    ingredients_json CLOB,
    missing_ingredients_json CLOB,
    nutrition_per_serving_json CLOB,
    tags_json CLOB,
    main_ingredients_json CLOB,
    source VARCHAR(100),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE week_plan (
    id UUID PRIMARY KEY,
    week_start DATE NOT NULL UNIQUE,
    week_end DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    confirmed_recipe_id UUID,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_week_plan_confirmed_recipe
        FOREIGN KEY (confirmed_recipe_id) REFERENCES recipe(id)
);

CREATE TABLE week_candidate (
    id UUID PRIMARY KEY,
    week_plan_id UUID NOT NULL,
    recipe_id UUID NOT NULL,
    candidate_group_version INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_week_candidate_week
        FOREIGN KEY (week_plan_id) REFERENCES week_plan(id),
    CONSTRAINT fk_week_candidate_recipe
        FOREIGN KEY (recipe_id) REFERENCES recipe(id)
);

CREATE TABLE pantry_item (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    quantity DOUBLE,
    unit VARCHAR(50),
    storage_type VARCHAR(20),
    purchased_at DATE,
    shelf_life_days_predicted INT,
    expires_at_predicted DATE,
    expires_at_override DATE,
    expire_confidence DOUBLE,
    expire_assumptions CLOB,
    expire_estimated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE shopping_item (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    quantity DOUBLE,
    unit VARCHAR(50),
    checked BOOLEAN NOT NULL,
    shelf_life_days_hint INT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE cook_log (
    id UUID PRIMARY KEY,
    cooked_at TIMESTAMP NOT NULL,
    recipe_id UUID,
    servings INT,
    nutrition_total_json CLOB,
    nutrition_per_serving_json CLOB,
    tags_json CLOB,
    main_ingredients_json CLOB,
    CONSTRAINT fk_cook_log_recipe
        FOREIGN KEY (recipe_id) REFERENCES recipe(id)
);
