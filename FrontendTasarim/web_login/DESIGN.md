---
name: Precision Minimalist System
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
  secondary: '#5f5e5e'
  on-secondary: '#ffffff'
  secondary-container: '#e5e2e1'
  on-secondary-container: '#656464'
  tertiary: '#3c3d3d'
  on-tertiary: '#ffffff'
  tertiary-container: '#545454'
  on-tertiary-container: '#cac8c8'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffdad4'
  primary-fixed-dim: '#ffb4a7'
  on-primary-fixed: '#400200'
  on-primary-fixed-variant: '#910b00'
  secondary-fixed: '#e5e2e1'
  secondary-fixed-dim: '#c8c6c5'
  on-secondary-fixed: '#1c1b1b'
  on-secondary-fixed-variant: '#474646'
  tertiary-fixed: '#e3e2e2'
  tertiary-fixed-dim: '#c7c6c6'
  on-tertiary-fixed: '#1b1c1c'
  on-tertiary-fixed-variant: '#464747'
  background: '#f9f9fe'
  on-background: '#1a1c1f'
  surface-variant: '#e2e2e7'
typography:
  headline-xl:
    fontFamily: Outfit
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Outfit
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Outfit
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  headline-sm:
    fontFamily: Outfit
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: DM Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: DM Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-md-bold:
    fontFamily: DM Sans
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
  label-sm:
    fontFamily: DM Sans
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.02em
  caption:
    fontFamily: DM Sans
    fontSize: 11px
    fontWeight: '400'
    lineHeight: 14px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  sidebar-width: 72px
  base-unit: 8px
  gutter: 16px
  margin-page: 32px
  stack-sm: 4px
  stack-md: 12px
  stack-lg: 24px
---

## Brand & Style

This design system is rooted in the principles of Swiss Minimalism and International Typographic Style. It is engineered for high-stakes ticket management where speed, clarity, and objectivity are paramount. The aesthetic is hyper-functional and macOS-inspired, stripping away all non-essential ornamentation to focus entirely on data integrity and user intent.

The brand personality is authoritative and precise. By utilizing a strictly flat architecture with zero shadows, gradients, or 3D effects, the system communicates a sense of modern reliability and "software as a tool." The interface remains neutral and quiet, allowing the blood-red primary color to serve as a high-contrast signal for urgency and action.

## Colors

The palette is strictly achromatic with the exception of the primary status color. 

- **Primary (#AA1101):** A deep blood red used exclusively for primary actions, critical alerts, and active states. It provides a sharp, authoritative focal point against the neutral backdrop.
- **Text Hierarchy:** #111111 is used for maximum legibility in headings and body text, while #737373 is reserved for metadata and de-emphasized labels.
- **Surface & Background:** The layout uses a two-tier white system. #FCFCFC acts as the global canvas and sidebar background, while #FFFFFF is used for the main content surfaces to create a subtle distinction without using elevation.
- **Borders:** All structural separation is handled by a 1px solid #E5E5EA border.

## Typography

The typography strategy pairs the geometric confidence of **Outfit** for headlines with the clean, utilitarian readability of **DM Sans** for all functional text and data.

- **Headlines:** Set in Outfit with a 600 weight. Letter spacing is slightly tightened on larger sizes to maintain the "Swiss" density.
- **Body:** DM Sans is used for its high x-height and neutral character. Weight 400 is standard for ticket descriptions, while weight 500 is used for UI labels and interactive elements to ensure they are distinguishable at a glance.
- **Hierarchy:** Contrast is achieved through size and weight rather than color. Maintain strict vertical rhythm to support the objective feel of the design system.

## Layout & Spacing

The layout is governed by a rigid 8px grid system, emphasizing density and information accessibility. 

- **Sidebar:** A fixed 72px vertical navigation bar sits on the left. It features an icons-only approach to minimize cognitive load, using a #FCFCFC background and a 1px solid #E5E5EA right border to separate it from the workspace.
- **Content Area:** Uses a fluid grid with a fixed 32px outer margin. Internal modules are separated by 1px borders rather than gutters to maximize horizontal space for ticket data.
- **Data Density:** High-density spacing is preferred. List items should have a compact vertical height to allow more tickets to be visible above the fold.

## Elevation & Depth

This design system explicitly prohibits the use of shadows, blurs, or gradients to create depth. Instead, depth is conveyed through **Tonal Layering** and **Line Work**.

1.  **Level 0 (Background):** #FCFCFC serves as the foundation.
2.  **Level 1 (Workspace):** #FFFFFF surfaces are placed on top of the background to define the primary work area.
3.  **Separation:** All hierarchical boundaries are defined by 1px solid borders in #E5E5EA. 
4.  **Interaction:** Hover states are indicated by a subtle shift to #F4F4F5. 

By removing shadows, the UI feels lightweight and fast, mirroring the mental state required for efficient support ticket processing.

## Shapes

The shape language is disciplined and consistent. 

- **Corner Radius:** A universal 8px (0.5rem) radius is applied to all buttons, containers, and cards. This softens the rigid grid just enough to feel modern and "macOS-like" without sacrificing the professional tone.
- **Borders:** Every container and interactive component (excluding underline-only inputs) must feature a 1px solid border. This replaces elevation as the primary method of containment.
- **Icons:** Should follow a medium-stroke weight (approx 1.5pt to 2pt) with slightly rounded terminals to match the 8px corner language of the containers.

## Components

### Buttons
- **Primary:** Solid #AA1101 background with #FFFFFF text. 8px radius. No shadow.
- **Secondary:** #FFFFFF background with a 1px solid #E5E5EA border. #111111 text.
- **Tertiary/Ghost:** No border or background. #737373 text, switching to #111111 on hover.

### Input Fields
- **Style:** Underline-only. A 1px solid #E5E5EA bottom border that transitions to 2px solid #AA1101 upon focus.
- **Labels:** Small, all-caps or title case DM Sans (500 weight) positioned above the underline.

### Cards & Containers
- Cards do not have shadows. They are defined by a 1px solid #E5E5EA border and a #FFFFFF background.
- Headers within containers should be separated by a 1px horizontal rule.

### Sidebar Icons
- Centered within the 72px width.
- Active state: The icon color shifts to #AA1101, or a 3px vertical "pill" indicator is placed against the left edge of the sidebar.

### Chips & Tags
- Rectangular with an 8px radius. 
- Background: #F4F4F5.
- Border: 1px solid #E5E5EA.
- High-priority tags use a light tint of the primary red with #AA1101 text.