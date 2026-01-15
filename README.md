# RecipeMaker

週単位でレシピ候補を生成・管理するための土台プロジェクトです。

## 起動手順

```bash
mvn spring-boot:run
```

## H2 Console

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/recipemaker`
- User: `sa`
- Password: (空)

## マイグレーション

起動時にFlywayが自動で `src/main/resources/db/migration` のSQLを適用します。

## 週ステータス遷移

- `NOT_CREATED` → (generate) → `GENERATING` → (confirm) → `CONFIRMED`
- `GENERATING` 状態では再生成 (`regenerate`) 可能

## 主要API

### WeekPlan
- `GET /api/weeks?month=YYYY-MM` : 週一覧取得。存在しない週は自動生成されます。
- `GET /api/weeks/{weekStart}` : 週詳細。
- `POST /api/weeks/{weekStart}/generate` : 週候補生成。
- `POST /api/weeks/{weekStart}/regenerate` : 週候補再生成。
- `POST /api/weeks/{weekStart}/confirm` : 確定 (body: `{ "recipeId": "..." }`).

### Recipe
- `GET /api/recipes/{id}`

### Pantry
- `GET /api/pantry`
- `POST /api/pantry`
- `PUT /api/pantry/{id}`
- `DELETE /api/pantry/{id}`

### Shopping
- `GET /api/shopping`
- `POST /api/shopping`
- `PUT /api/shopping/{id}`
- `POST /api/shopping/{id}/move-to-pantry`

### CookLog / Health
- `GET /api/history?from=YYYY-MM-DD&to=YYYY-MM-DD`
- `POST /api/history`
- `GET /api/health/summary?days=14`

## 画面URL

- `/`
- `/weeks/{weekStart}`
- `/pantry`
- `/shopping`
- `/history`
- `/health`
