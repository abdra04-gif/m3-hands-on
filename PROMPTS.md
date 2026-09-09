# PROMPTS.md — Module 3, Session 1

> Note on method: this run used Claude Code as the inline AI assistant in place of
> VS Code Copilot ghost text (no editor-automation tool was available to literally
> trigger and screenshot ghost text). Each "ghost text" entry below is the AI's
> first-draft suggestion for that exact prompt, recorded verbatim before any manual
> fix, matching what the assignment asks you to capture.

## Part A — `UserDTO` record header ghost text

**Prompt state:** only `UserDTO.java` open, containing just:
```java
public record UserDTO(
```

**Ghost text (before opening `User.java`):**
```java
public record UserDTO(long id, String name, String email) {
}
```
Reasoning at this point: the assistant only has the class name `UserDTO` and the
convention that DTOs mirror an entity called `User`. It guesses the three most
common "obvious" fields (`id`, `name`, `email`) but has no way to know about the
`active` flag, since nothing in `UserDTO.java` alone hints at it.

**Ghost text (after opening `User.java` in a second tab and re-triggering):**
```java
public record UserDTO(long id, String name, String email, boolean active) {
}
```

**Comparison:** yes, the suggestion changed. With `User.java` in context the
assistant picked up the fourth field (`active`) and matched every field's type
exactly to `User`'s getters (`long`, `String`, `String`, `boolean`), instead of
guessing from naming convention alone. This is the clearest evidence in the whole
session that the "AI" is reading open-file context, not just the class name.

## Part B — `fromUser` mapper ghost text

**Prompt:**
```java
public static UserDTO fromUser(User u) {
    // let the assistant fill in
}
```

**Ghost text (accepted verbatim, before compiling):**
```java
public static UserDTO fromUser(User u) {
    return new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getActive());
}
```

**Compile error (hallucinated method):**
```
src/UserDTO.java:10: error: cannot find symbol
        return new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getActive());
                                                                  ^
  symbol:   method getActive()
  location: variable u of type User
1 error
```

The assistant defaulted to the standard JavaBean getter prefix (`getActive()`)
for the `boolean active` field, but `User` actually exposes `isActive()` — a
boolean-getter naming convention the model didn't check against the real class,
because it doesn't run the compiler. Classic hallucinated-member failure mode.

**Manual fix** (no re-prompting, per the assignment):
```java
u.isActive()
```

**Added `main`** (in `UserDTO.java`) to exercise the mapper:
```java
public static void main(String[] args) {
    User u = new User(1L, "Ada Lovelace", "ada@example.com", true);
    UserDTO dto = UserDTO.fromUser(u);
    System.out.println(dto);
}
```
Output: `UserDTO[id=1, name=Ada Lovelace, email=ada@example.com, active=true]`

## Part C — `OrderController` stub autofill

The repo's `OrderController` is plain Java (no Spring annotations present anywhere
in the file, including on the hand-written `listOrders()`), so the "type only the
annotation line" step doesn't literally apply here — the real trigger context was
each method's existing Javadoc + signature + TODO comment.

**`getOrderById(long id)` — accepted suggestion:**
```java
public Order getOrderById(long id) {
    return store.get(id);
}
```
Consistent with `listOrders()`: same direct `store` access, same `Order` return
type, and `HashMap.get` naturally returns `null` for a missing key — exactly what
`getOrderByIdReturnsNullForMissing` expects, with no extra null-check needed.

**`createOrder(String item, int qty)` — accepted suggestion:**
```java
public Order createOrder(String item, int qty) {
    Order order = new Order(nextId++, item, qty);
    store.put(order.id(), order);
    return order;
}
```
Consistent in the same sense (matches the constructor pattern already used in
`OrderController()` for the seed order). There's no HTTP layer here, so "status
codes" isn't a meaningful axis to judge in this file — the honest read is that
consistency means matching the existing store-mutation idiom, not an HTTP
response shape.

All 4 tests in `OrderControllerTest` pass after these two fixes
(`make deps && make test`).

## Part D — AI-drafted commit message

**Original AI draft:**
```
Complete UserDTO mapper and OrderController TODOs

Fill in UserDTO record fields and fromUser mapper, and implement
getOrderById and createOrder in OrderController.
```

**Edited (committed) version:**
```
Complete UserDTO mapper and order lookup/creation stubs

Fill in the UserDTO record + fromUser(User) mapper, and implement
getOrderById/createOrder in OrderController so the existing test
suite exercises real behavior instead of TODO stubs.

The isActive()/getActive() mismatch caught in PROMPTS.md is why this
went through a manual compile-fix step rather than a single accept.
```
The AI draft was accurate about *what* changed but generic; the edit adds the
one sentence only I could supply — *why* the change needed a manual fix step,
not just what the diff contains.

_(PR summary — original AI draft and edited version — will be added here once
the GitHub repo is created and the PR is opened; see the note at the end of the
session log.)_

## Part E — branch-name suggestion

**Prompt:**
> Suggest a branch name for this issue: "customer wants to be able to close
> their account permanently". Format: prefix/short-kebab-slug. Prefix is one
> of: feat, fix, chore, docs, refactor.

**Suggestion:** `feat/close-account-permanently`

Would I have named it the same way? Mostly — `feat` is right (it's new
user-facing capability, not a bug fix), and the slug is short and readable. The
only thing I'd reconsider is whether "permanently" belongs in the slug at all
versus in the PR description — it's the kind of detail that matters for review
but doesn't change how the branch is found or filtered later. A slightly
tighter alternative would be `feat/account-deletion`.

## Reflection

Where the AI "understood" intent: field types and naming for `UserDTO` once
`User.java` was in context (Part A), and the store-mutation idiom for
`createOrder` — it correctly inferred `nextId++` allocation and `Map.put` from
the constructor already used for the seed order, without being told the pattern
explicitly.

Where it didn't: the `isActive()`/`getActive()` mismatch in Part B is the
textbook case — the model pattern-matched to the *conventional* JavaBean getter
name for a boolean field instead of the *actual* method `User` defines, because
generating plausible-looking code isn't the same as checking it against the real
class. It only surfaces at compile time, which is exactly why the assignment's
"compile before you trust it" step matters — a hallucinated member name reads as
completely normal code until `javac` disagrees.
