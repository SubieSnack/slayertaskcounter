\# Mob KC Overlay (mobkcoverlay)



\## Description

Mob KC Overlay is a RuneLite plugin that tracks kill-counts for any NPC you interact with.  

You can choose the NPC manually or enable automatic switching so the plugin follows whatever you attack.  

An overlay displays the current mob name and total kills, with optional animations and customization.



\## Features

\- Right-click any NPC → \*\*Show KC\*\* to instantly switch tracking  

\- (Optional) Automatic mob detection — switches to the NPC you're attacking  

\- Overlay shows:  

&nbsp; - Current tracked mob  

&nbsp; - Total kill count  

&nbsp; - Optional +1 fade animation  

\- Manual kill-count adjustment (+ or −) in the config  

\- Per-mob KC is saved independently  

\- Dynamic overlay size to fit long NPC names  

\- Plugin Hub–ready structure and clean codebase  



\## Installation (Local)

Until published in the Plugin Hub, you can install manually:



1\. Build the `.jar` using `./gradlew build`

2\. Open RuneLite → \*Settings\* → \*Plugins\*  

3\. Click the gear icon → \*\*Open plugin folder\*\*

4\. Drop the built `.jar` inside that folder  

5\. Restart RuneLite  

6\. Enable \*\*Mob KC Overlay\*\* from the plugin list



\## For Plugin Hub Submission

To submit to RuneLite’s Plugin Hub:



1\. Ensure the project includes:  

&nbsp;  - `build.gradle`  

&nbsp;  - `settings.gradle`  

&nbsp;  - `runelite-plugin.properties`  

&nbsp;  - `icon.png`  

&nbsp;  - `LICENSE`  

&nbsp;  - Clean code with proper annotations  

2\. Create a fork of the official Plugin Hub repo  

3\. Add your plugin under `/plugins/mobkcoverlay`  

4\. Fill out `externalPlugin.json` (repository, commit hash, icon, etc.)  

5\. Submit a pull request with a clear explanation  

6\. Wait for RuneLite maintainers to review



\## Usage

1\. Enable the plugin  

2\. Choose whether you want:  

&nbsp;  - Manual NPC selection  

&nbsp;  - Or auto-switch mode  

3\. Kill mobs that match your tracked name  

4\. Watch your KC increase in real time  

5\. Customize text colour, animation settings, and overlay behavior  

6\. Use manual KC adjustment if needed



\## Support

If you encounter issues or want to request features, open an issue on GitHub:  

https://github.com/SubieSnack/mobkcoverlay



\## License

This project is licensed under the BSD-2 Clause License.  

See the `LICENSE` file for details.



