# Design System Strategy: The Digital Sanctuary

## 1. Overview & Creative North Star
The Creative North Star for this design system is **"The Digital Sanctuary."** 

This system is designed to transcend the utilitarian nature of mobile apps, transforming the interface into a serene, meditative space. We achieve this by moving away from rigid, "app-like" grids toward an editorial, high-end experience. By utilizing expansive white space (breathing room), intentional asymmetry, and a deep forest palette, we create a professional yet spiritual environment. 

The design intentionally breaks the "template" look through overlapping elements—such as hero cards that bleed into background gradients—and a sophisticated typographic scale that privileges readability and calm over information density.

---

## 2. Color Philosophy
Our palette is rooted in the natural world, using tonal greens and mints to evoke growth, peace, and stability.

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using 1px solid borders to define sections or cards. 
Boundaries must be defined solely through background color shifts. For example, a card using `surface-container-lowest` (#ffffff) should sit on a `surface` (#f6faf7) or `surface-container-low` (#f1f5f2) background. Contrast is achieved through tonal transitions, not structural lines.

### Surface Hierarchy & Nesting
Treat the UI as a series of physical layers, similar to stacked sheets of fine, heavy-weight paper.
- **Base Layer:** `background` (#f6faf7) or `surface`.
- **Primary Containers:** `surface-container` (#ebefec) for large content blocks.
- **Interactive/Floating Elements:** `surface-container-lowest` (#ffffff) to provide the highest "lift."

### Glass & Gradient Rule
To ensure a premium feel, avoid "flat" green blocks. Use subtle linear gradients for main CTAs and Hero sections, transitioning from `primary` (#00450d) to `primary_container` (#1b5e20). For floating navigation or temporary overlays, use **Glassmorphism**: semi-transparent surface colors with a `20px` to `40px` backdrop-blur to allow the background sanctuary colors to bleed through.

---

## 3. Typography
The typography is a dialogue between the modern, authoritative **Manrope** and the approachable, clean **Plus Jakarta Sans**.

- **Display & Headlines (Manrope):** These are the "voice" of the sanctuary. Use `display-lg` and `headline-lg` with generous tracking to create a sense of importance and editorial polish.
- **Body & Titles (Plus Jakarta Sans):** Chosen for its exceptional legibility. `body-lg` is the standard for long-form reflection, while `title-sm` provides clear, professional navigation headers.
- **Brand Identity:** The contrast between the bold, geometric headlines and the soft, humanistic body text conveys a brand that is both technically professional and spiritually grounded.

---

## 4. Elevation & Depth
We reject traditional "Material" drop shadows. Depth in this system is achieved through **Tonal Layering**.

- **The Layering Principle:** Place a white container (`surface-container-lowest`) onto a mint section (`surface-container-low`) to create a natural, soft lift.
- **Ambient Shadows:** If an element must float (e.g., a bottom navigation bar or a critical modal), use a "Sanctuary Shadow": 
  - **Blur:** 32px - 64px.
  - **Opacity:** 4% - 6%.
  - **Color:** Use a tinted version of `on-surface` (a deep, desaturated green) rather than pure black.
- **The "Ghost Border" Fallback:** If accessibility requires a container definition, use the `outline-variant` token at **15% opacity**. Never use 100% opaque borders.

---

## 5. Components

### Buttons
- **Primary:** High-contrast `primary` (#00450d) with `on-primary` (#ffffff) text. Use a slight gradient and `DEFAULT` (0.5rem) or `full` roundedness.
- **Secondary:** `secondary_container` (#cae8c2) with `on-secondary_container` (#4f694b). These should feel integrated into the background.

### Cards & Lists
- **Forbid Divider Lines:** Use `2.5rem` (spacing-10) of vertical whitespace to separate list items, or alternating subtle background shifts between `surface` and `surface-container-low`.
- **Editorial Cards:** Hero cards should use `xl` (1.5rem) corner radius and include an icon or image that overlaps the container boundary slightly to break the "box" feel.

### Input Fields
- Avoid "boxed" inputs. Use a "Soft Underline" or a solid `surface-container-high` background with no border. Focus states are signaled by a transition to `primary` (#00450d) text and a subtle 2px bottom accent.

### Sanctuary-Specific Components
- **Progress Rings:** Use `primary` for the active path and `surface-variant` for the track to visualize spiritual streaks or journey completion.
- **Prayer Timers:** Large `display-md` typography paired with `primary-fixed-dim` backgrounds to create a high-visibility, low-stress focal point.

---

## 6. Do's and Don'ts

### Do
- **DO** use the `12` (3rem) and `16` (4rem) spacing tokens for top-level page margins to create a high-end editorial feel.
- **DO** use `surface_bright` for areas meant to draw the eye without using high-intensity color.
- **DO** lean into asymmetry. Align text to the left while placing supporting icons or imagery to the far right with significant white space between them.

### Don'ts
- **DON'T** use pure black (#000000) for text. Always use `on-surface` (#181d1b) to maintain the soft, organic feel.
- **DON'T** use standard "system" shadows. They feel "cheap" and break the sanctuary aesthetic.
- **DON'T** crowd the interface. If a screen feels full, increase the spacing tokens and move secondary information to a nested "surface-container."
- **DON'T** use high-contrast dividers. A change in background tint is always preferred over a line.