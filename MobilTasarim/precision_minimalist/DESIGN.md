---
name: Precision Minimalist
colors:
  surface: '#f9f9fe'
  surface-dim: '#d9dade'
  surface-bright: '#f9f9fe'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f3f3f8'
  surface-container: '#ededf2'
  surface-container-high: '#e8e8ed'
  surface-container-highest: '#e2e2e7'
  on-surface: '#1a1c1f'
  on-surface-variant: '#5b403b'
  inverse-surface: '#2f3034'
  inverse-on-surface: '#f0f0f5'
  outline: '#90706a'
  outline-variant: '#e4beb7'
  surface-tint: '#b91e0b'
  primary: '#7f0900'
  on-primary: '#ffffff'
  primary-container: '#aa1101'
  on-primary-container: '#ffb8ab'
  inverse-primary: '#ffb4a7'
  secondary: '#5e5e5e'
  on-secondary: '#ffffff'
  secondary-container: '#e3e2e2'
  on-secondary-container: '#646464'
  tertiary: '#3e3d3d'
  on-tertiary: '#ffffff'
  tertiary-container: '#555454'
  on-tertiary-container: '#cbc8c8'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffdad4'
  primary-fixed-dim: '#ffb4a7'
  on-primary-fixed: '#400200'
  on-primary-fixed-variant: '#910b00'
  secondary-fixed: '#e3e2e2'
  secondary-fixed-dim: '#c7c6c6'
  on-secondary-fixed: '#1b1c1c'
  on-secondary-fixed-variant: '#464747'
  tertiary-fixed: '#e5e2e1'
  tertiary-fixed-dim: '#c8c6c5'
  on-tertiary-fixed: '#1c1b1b'
  on-tertiary-fixed-variant: '#474646'
  background: '#f9f9fe'
  on-background: '#1a1c1f'
  surface-variant: '#e2e2e7'
typography:
  display:
    fontFamily: Outfit
    fontSize: 48px
    fontWeight: '600'
    lineHeight: '1.1'
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Outfit
    fontSize: 32px
    fontWeight: '600'
    lineHeight: '1.2'
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Outfit
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.2'
  headline-md:
    fontFamily: Outfit
    fontSize: 20px
    fontWeight: '500'
    lineHeight: '1.4'
  stats-lg:
    fontFamily: Outfit
    fontSize: 36px
    fontWeight: '600'
    lineHeight: '1'
    letterSpacing: -0.02em
  body-lg:
    fontFamily: DM Sans
    fontSize: 18px
    fontWeight: '400'
    lineHeight: '1.6'
  body-md:
    fontFamily: DM Sans
    fontSize: 15px
    fontWeight: '400'
    lineHeight: '1.5'
  label-sm:
    fontFamily: DM Sans
    fontSize: 13px
    fontWeight: '500'
    lineHeight: '1.2'
    letterSpacing: 0.02em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 4px
  container-margin: 24px
  gutter: 16px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 32px
---

## Brand & Style
The design system is rooted in Swiss Minimalism fused with the fluid, polished execution of modern macOS interfaces. The personality is hyper-functional, objective, and authoritative, prioritizing information density and clarity over decorative elements.

The aesthetic avoids all simulated depth—no drop shadows, gradients, or 3D effects are permitted. Instead, hierarchy is established through meticulous typography, strategic use of whitespace, and 1px "hairline" borders. The emotional response should be one of professional reliability and "quiet power," where the interface recedes to let the data and actions take center stage.

## Colors
The palette is restricted and intentional.
- **Primary (#AA1101):** "Blood Red" is reserved exclusively for high-priority actions, critical status indicators, and subtle branding accents. It must be used sparingly to maintain its impact.
- **Tertiary (#111111):** Used for all primary text and core iconography to ensure maximum contrast against the white surfaces.
- **Secondary (#737373):** Dedicated to metadata, supporting text, and inactive icons.
- **Neutral (#E5E5EA):** The structural backbone of the UI, used for all borders, separators, and inactive component backgrounds.
- **Surfaces:** A two-tier white system. The main canvas is `#FCFCFC`, while interactive cards and foreground containers use pure `#FFFFFF` to create a subtle perceived lift without using shadows.

## Typography
The system employs a dual-font strategy to balance character with utility.
- **Outfit** is used for all "at-a-glance" information, including headings and numerical statistics. Its geometric precision reinforces the Swiss aesthetic.
- **DM Sans** handles all long-form reading and functional labels. It provides high legibility at smaller scales and a neutral tone that doesn't distract from the content.

Tighten letter spacing on larger display sizes to maintain a "locked-in" professional look. Use `label-sm` in all-caps only for very small metadata tags.

## Layout & Spacing
The layout follows a strict 4px grid system.
- **Structure:** Use a 12-column fluid grid for desktop and a single column for mobile.
- **Separation:** Do not use background color blocks to separate sections. Use 1px `#E5E5EA` horizontal or vertical lines.
- **Whitespace:** Emphasize "macro-white-space" between major content blocks to create a sense of calm and focus.
- **Margins:** Desktop containers should maintain a minimum 24px margin from the viewport edge. On mobile, this reduces to 16px.

## Elevation & Depth
Depth is entirely flat.
- **Tonal Layering:** Objects do not "float." Instead, they sit on the surface.
- **Borders:** All containers, cards, and modal windows are defined by a 1px solid border in `#E5E5EA`.
- **Active States:** Instead of shadows, indicate active or focused states using a color change of the border to `#111111` or by applying a very subtle `#F4F4F5` background tint to the element.

## Shapes
Following the macOS smoothness principle, the system uses a consistent **8px (0.5rem)** corner radius for all primary containers, including buttons, input fields, and cards. This softens the brutalism of the 1px borders, making the interface feel modern and approachable. Small components like tags or chips may use a fully rounded "pill" shape to distinguish them from actionable buttons.

## Components
- **Input Fields:** Use an "underline-only" style. The field consists of a label in `label-sm` (Secondary color) and an input area defined by a 1px bottom border. On focus, the bottom border transitions to `#111111`.
- **Buttons (Primary):** Solid `#AA1101` background with White text. No border. 8px radius.
- **Buttons (Secondary/Destructive):** White or transparent background with a 1px `#E5E5EA` border. Icons and text within these buttons use `#AA1101` if the action is destructive, or `#111111` for standard secondary actions.
- **Cards:** Pure `#FFFFFF` background, 1px `#E5E5EA` border, 8px radius. Content inside should have 20px - 24px of internal padding.
- **Chips/Status:** Small, low-contrast pills. Use a light gray background (`#F4F4F5`) with `label-sm` text. Only use the Primary Red for "Critical" or "Live" status chips.
- **Lists:** Rows separated by 1px horizontal lines. High density with 12px vertical padding per row.
