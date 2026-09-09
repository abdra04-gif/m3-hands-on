# PROMPTS.md — Module 3, Session 1

Quick note before the log: I did this with Claude Code sitting in for Copilot's
inline ghost text — there's no way for it to literally trigger and screenshot
VS Code's suggestion popup, so it played "the assistant" itself and I wrote down
whatever it produced as the suggestion, warts and all, before touching anything.
Everything below is what actually happened, in the order it happened.

## Part A — the empty `UserDTO(` header

Started with just:
```java
public record UserDTO(
```
and nothing else open.

First suggestion, with only `UserDTO.java` in view:
```java
public record UserDTO(long id, String name, String email) {
}
```
Makes sense when you think about it — all it has to go on is the name `UserDTO`
and the general pattern that a DTO shadows some entity called `User`. It guessed
the three fields everybody's User class has (id/name/email) and just... stopped
there. No way it could've known about the `active` flag from an empty header
alone.

Then I opened `User.java` in a second tab and re-triggered it. New suggestion:
```java
public record UserDTO(long id, String name, String email, boolean active) {
}
```

So yes — it changed, and not just cosmetically. Once `User.java` was visible it
picked up the fourth field and got every type right (`long`, `String`, `String`,
`boolean`) instead of guessing. That's the most convincing bit of "it's actually
reading my open tabs" evidence from the whole exercise.

## Part B — the mapper, and where it went wrong

Prompt:
```java
public static UserDTO fromUser(User u) {
    // let the assistant fill in
}
```

What it handed back, accepted as-is before I even tried to compile:
```java
public static UserDTO fromUser(User u) {
    return new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getActive());
}
```

Ran `make build` and got this:
```
src/UserDTO.java:10: error: cannot find symbol
        return new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getActive());
                                                                  ^
  symbol:   method getActive()
  location: variable u of type User
1 error
```

Exactly the trap the assignment warns about. `User` has `isActive()`, not
`getActive()` — the model just reached for the standard JavaBean `getX()`
pattern for a boolean field, which is a totally reasonable guess *in general*
but wrong for this specific class. It never checked against the real method,
because generating code isn't the same as running it.

Fixed it by hand, no re-prompting:
```java
u.isActive()
```

Then added a small `main` to actually see it work:
```java
public static void main(String[] args) {
    User u = new User(1L, "Ada Lovelace", "ada@example.com", true);
    UserDTO dto = UserDTO.fromUser(u);
    System.out.println(dto);
}
```
Prints: `UserDTO[id=1, name=Ada Lovelace, email=ada@example.com, active=true]`

## Part C — the two controller stubs

Small wrinkle here: `OrderController` is plain Java, no Spring annotations
anywhere, not even on the hand-written `listOrders()`. So "type the annotation
line and let it fill in the body" doesn't really apply — what actually drove the
suggestion was the existing Javadoc, the method signature, and the TODO comment.

`getOrderById`:
```java
public Order getOrderById(long id) {
    return store.get(id);
}
```
This lines up with `listOrders()` in the ways that matter — same direct access
to `store`, same `Order` return type — and `HashMap.get` already returns `null`
on a miss, which is exactly what `getOrderByIdReturnsNullForMissing` expects.
Didn't even need an extra null check.

`createOrder`:
```java
public Order createOrder(String item, int qty) {
    Order order = new Order(nextId++, item, qty);
    store.put(order.id(), order);
    return order;
}
```
Same story — matches the pattern already used in the constructor for the seed
order. There's no HTTP layer in this file at all, so checking "are status codes
consistent" doesn't really make sense here; the honest version of that question
is just "does it follow the same store-mutation idiom," and it does.

Ran `make deps && make test` afterward — all 4 tests green.

## Part D — commit message

AI's first pass:
```
Complete UserDTO mapper and OrderController TODOs

Fill in UserDTO record fields and fromUser mapper, and implement
getOrderById and createOrder in OrderController.
```

Not wrong, just flat — it's a bullet-point description of the diff with no
opinion in it. What I actually committed:
```
Complete UserDTO mapper and order lookup/creation stubs

Fill in the UserDTO record + fromUser(User) mapper, and implement
getOrderById/createOrder in OrderController so the existing test
suite exercises real behavior instead of TODO stubs.

The isActive()/getActive() mismatch caught in PROMPTS.md is why this
went through a manual compile-fix step rather than a single accept.
```
The AI can read the diff and tell you *what* moved. It can't tell you *why* it
took an extra step to get there — that sentence about the isActive()/getActive()
mismatch is the only part of the message that actually required a human.

## Part D, continued — PR description

Repo: https://github.com/abdra04-gif/m3-hands-on
PR: https://github.com/abdra04-gif/m3-hands-on/pull/1

First draft, straight off the diff:
```
## Summary
- Added fields and fromUser mapper to UserDTO record
- Implemented getOrderById and createOrder in OrderController
- Added PROMPTS.md documenting the AI-assisted workflow

## Test plan
- Ran make test
```

What went into the actual PR:
```
## Summary
- Complete UserDTO as a record (id, name, email, active) with a
  fromUser(User) mapper and a small main that exercises it
- Implement the getOrderById and createOrder TODO stubs in
  OrderController
- Add PROMPTS.md documenting the AI ghost-text suggestions used for
  each piece, including a hallucinated getActive() call that failed
  to compile and was fixed by hand to isActive()

## Test plan
- [x] make deps && make test — all 4 tests in OrderControllerTest pass
- [x] java -cp build UserDTO — mapper prints the expected
      UserDTO[id=1, name=Ada Lovelace, email=ada@example.com, active=true]
- [x] Reviewed PROMPTS.md for the compile error caught during Part B
      and confirmed the fix matches User's actual isActive() accessor
```
Same gap as the commit message, basically: the generic draft tells a reviewer
what changed, the edited one tells them what to actually go check and why they
should trust it.

## Part E — branch name for a hypothetical issue

Asked: "Suggest a branch name for this issue: customer wants to be able to
close their account permanently."

Got back: `feat/close-account-permanently`

Would I have picked the same thing? Pretty much, yeah — `feat` is the right
prefix since this is new capability rather than a fix, and the slug is short
enough to read at a glance. If I'm nitpicking, "permanently" is more of a PR-
description detail than something that needs to live in the branch name — I'd
probably have just gone with `feat/account-deletion` and let the description
carry the nuance.

## Reflection

The model was genuinely good at picking up context it could see directly —
grabbing the `active` field once `User.java` was open, matching types field for
field, inferring the `nextId++` / `store.put` pattern for `createOrder` from the
constructor that was already sitting right there in the same file. It didn't
need those patterns spelled out.

Where it fell down was exactly the case the assignment is pointing at: it
reached for `getActive()` because that's the *statistically normal* getter name
for a boolean field, not because it checked what `User` actually exposes. That's
the core difference between generating plausible code and generating correct
code — the model doesn't run a compiler in its head, so a hallucinated member
name looks completely fine right up until `javac` disagrees with it. Which is
basically the whole lesson of this lab: read before you accept, and compile
before you trust it.
