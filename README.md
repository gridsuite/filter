# filter

[![Actions Status](https://github.com/gridsuite/filter/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/gridsuite/filter/actions)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=org.gridsuite%3Afilter&metric=coverage)](https://sonarcloud.io/component_measures?id=org.gridsuite%3Afilter&metric=coverage)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)

## Description

The **gridsuite-filter** library is the evaluation engine for filters in the [GridSuite](https://github.com/gridsuite) platform.

It is the **shared vocabulary (types, enums) and evaluation engine** for anything in the platform that needs to select network equipment by criteria. It is a **framework-agnostic Java library** (no Spring dependency) used across many services and libraries of the platform. It provides the following capabilities:

- **DTO model** for both filter types (`IDENTIFIER_LIST`, `EXPERT`), using Jackson polymorphic deserialization so both types are handled transparently by callers.
- **Evaluate filters** against a PowSyBl `Network`, returning a list of `FilterEquipments` — matched equipment with their IDs and types — regardless of the filter type.
- **`FilterLoader` interface**: a single-method SPI that decouples the library from any HTTP transport or persistence layer. Callers supply their own implementation to fetch filter definitions by UUID, enabling cross-filter references (e.g. `IS_PART_OF` rules in expert filters).

---

## Filter Types

### Identifier List (`IDENTIFIER_LIST`)

An explicit list of **equipment IDs** with an optional `distributionKey` per entry (used for example as weighting for sensitivity analysis). Evaluation looks up each ID directly in the network and reports which IDs were not found.

### Expert (`EXPERT`)

A **rule tree** composed of logical combinators (`AND`, `OR`) and leaf rules comparing network equipment fields (`NOMINAL_VOLTAGE`, `COUNTRY`, `MIN_P`, `MAX_P`, etc.) against values using operators (`EQUALS`, `IN`, `BETWEEN`, `GREATER_THAN`, `IS_PART_OF`, ...).

Expert filters support all network equipment types and can reference other filter UUIDs via `IS_PART_OF` / `IS_NOT_PART_OF` operators, resolved at evaluation time via the `FilterLoader`.

---


