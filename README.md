# cloud-itonami-isco-7318

Open Occupation Blueprint for **ISCO-08 7318**: Handicraft Workers in Textile, Leather and Related Materials.

**Maturity: `:implemented`** — CraftAdvisor ⊣
TextileLeatherHandicraftGovernor as a langgraph StateGraph
(`intake → advise → govern → decide → commit/hold`, human-approval
interrupt), modeled on cloud-itonami-isco-4311's bookkeeping actor.
17 tests / 33 assertions green. The governor never dispatches
hardware — it only gates what the material-handling robot below may
execute.

The craft/delivery HARD invariants — basis, arithmetic and set
coverage, not convenience:

1. **Material basis + stock ceiling** — a craft step's material must
   be a registered stock key, and the proposed quantity must not
   exceed the registered on-hand stock.
2. **Spec completeness** — a delivery's delivered-items set must be a
   superset of the order's registered required-spec-items set (partial
   delivery is not delivery).

`:approve-sharp-equipment-operation` and `:approve-chemical-treatment`
**always** escalate to human sign-off regardless of confidence, per
this repo's Trust Controls (business-model.md).

This repository designs a forkable OSS business for an independent textile/leather handicraft worker: a material-handling robot performs cutting-table setup and finished-piece transport under a governor-gated actor, so the practice keeps its own order and material records instead of renting a closed craft-business SaaS.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a material-handling robot performs cutting-table setup and finished-piece transport under an actor that proposes
actions and an independent **Textile Leather Handicraft Governor** that gates them. The governor never
dispatches hardware itself; `:high`/`:safety-critical` actions (such as
operating near sharp cutting equipment, or applying chemical leather-treatment agents) require human sign-off.

A live sample of the operator console (robotics safety console, shared template) is rendered in [docs/samples/operator-console.html](docs/samples/operator-console.html) — pure-data HTML output of `kotoba.robotics.ui`.

## Core Contract

```text
client order + material spec + craft protocol
        |
        v
Craft Advisor -> Textile Leather Handicraft Governor -> craft/finish, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, suppress
an operating record, or disclose sensitive data without governor approval and
audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `7318`). Required capabilities:

- :robotics
- :forms
- :audit-ledger
- :bpmn

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
