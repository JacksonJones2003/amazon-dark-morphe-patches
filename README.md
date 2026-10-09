# 🌙 Amazon dark mode patches

Patches for use with [Morphe](https://morphe.software) that add a dark mode to the
Amazon Shopping Android app (`com.amazon.mShop.android.shopping`).

## ❓ About

The Amazon Shopping app has no dark mode. The **Dark mode** patch adds one that follows
the system dark theme:

- Web pages shown in the app (nearly all of the app content) are darkened by Android WebView.
- Optionally (on by default), the native parts of the app such as the search bar and the bottom
  navigation bar are darkened by the Android "force dark" feature.

Requires Android 13 or newer for the web pages to be darkened.

These patches are not affiliated with Amazon or with the Morphe project.

### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=JacksonJones2003/amazon-dark-morphe-patches

Releases from the `dev` branch are pre-releases. Enable pre-releases for this patch source in Morphe Manager to use them.

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0-dev.1](https://github.com/JacksonJones2003/amazon-dark-morphe-patches/releases/tag/v1.0.0-dev.1)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;1 patches total
<details open>
<summary>📦 Amazon Shopping&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Dark mode](#dark-mode) | Adds a dark mode that follows the system dark theme. Web pages shown in the app are darkened, and optionally the native parts of the app. | • Darken native UI |

</details>

<!-- PATCHES_END -->

### 🛠️ Building locally

- Run `./gradlew buildAndroid`
- The built patches .mpp file is found in `patches/build/libs/patches-*.mpp`
- Patch the mpp file using [Morphe-Desktop](https://github.com/MorpheApp/morphe-desktop)
  like any other patch bundle.

See the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation) for more information.

## 📜 License

These patches are licensed under the [GNU General Public License v3.0](LICENSE)
