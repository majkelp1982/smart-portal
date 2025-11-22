# Setting Up AI Assistants in PyCharm/IntelliJ IDEA

This guide will help you set up AI-powered coding assistants (like Claude, GitHub Copilot, etc.) in your PyCharm or IntelliJ IDEA IDE.

## Prerequisites

- PyCharm Professional or IntelliJ IDEA (Community or Ultimate edition)
- An active account with your chosen AI assistant provider

## Available AI Assistant Options

### 1. GitHub Copilot

GitHub Copilot is an AI pair programmer that helps you write code faster.

**Installation:**
1. Open PyCharm/IntelliJ IDEA
2. Go to `File` → `Settings` (on Windows/Linux) or `IntelliJ IDEA` → `Preferences` (on macOS)
3. Navigate to `Plugins`
4. Click on the `Marketplace` tab
5. Search for "GitHub Copilot"
6. Click `Install` and restart the IDE
7. After restart, you'll be prompted to sign in to GitHub
8. Follow the authentication process

**Configuration:**
- Go to `Settings` → `Tools` → `GitHub Copilot`
- Enable/disable suggestions as needed
- Configure keyboard shortcuts if desired

### 2. Claude via Continue

Continue is an open-source AI code assistant that supports Claude and other LLMs.

**Installation:**
1. Go to `File` → `Settings` → `Plugins`
2. Search for "Continue" in the Marketplace
3. Install and restart the IDE
4. Configure your API key for Claude (Anthropic)

**Configuration:**
1. After installation, open Continue settings
2. Add your Anthropic API key
3. Select Claude as your model (e.g., claude-3-5-sonnet-20241022)
4. Configure your preferences

### 3. Tabnine

Tabnine is an AI code completion tool that runs locally or in the cloud.

**Installation:**
1. Go to `File` → `Settings` → `Plugins`
2. Search for "Tabnine" in the Marketplace
3. Install and restart the IDE
4. Sign up for a Tabnine account when prompted

### 4. Codeium

Codeium is a free AI-powered code completion tool.

**Installation:**
1. Go to `File` → `Settings` → `Plugins`
2. Search for "Codeium" in the Marketplace
3. Install and restart the IDE
4. Sign up for a free account when prompted

## Troubleshooting

### AI Assistant Not Working

1. **Check Plugin Installation:**
   - Go to `Settings` → `Plugins` → `Installed`
   - Verify the plugin is installed and enabled

2. **Verify API Keys:**
   - Make sure your API keys are correctly configured
   - Check if your subscription/credits are active

3. **Check Network Connection:**
   - Some AI assistants require internet connection
   - Verify your firewall isn't blocking the connection

4. **Update the Plugin:**
   - Go to `Settings` → `Plugins`
   - Check for updates and install if available

5. **Check IDE Version:**
   - Some plugins require specific IDE versions
   - Update your IDE if necessary

### Cannot Access Claude Specifically

If you're trying to use Claude (Anthropic's AI):

1. **Using Continue Plugin:**
   - Install the Continue plugin (see above)
   - Get an API key from https://console.anthropic.com/
   - Add the API key in Continue settings

2. **Using Other Integrations:**
   - Check if there are other Claude integration plugins in the marketplace
   - Some plugins may require a paid subscription

## Project-Specific Setup

This project is configured with:
- Java 17
- Maven build system
- Google Java code style formatting (via Spotless)

The IDE configurations in `.idea/` directory are now shared across the team to ensure consistent:
- Code styles
- Run configurations
- Inspection profiles
- VCS settings

## Getting API Keys

### Anthropic (Claude)
1. Visit https://console.anthropic.com/
2. Sign up or log in
3. Navigate to API Keys section
4. Generate a new API key
5. Copy and save it securely

### GitHub Copilot
1. Visit https://github.com/features/copilot
2. Start a free trial or subscribe
3. Authentication is handled through the plugin

## Additional Resources

- [JetBrains Plugin Marketplace](https://plugins.jetbrains.com/)
- [GitHub Copilot Documentation](https://docs.github.com/en/copilot)
- [Continue Documentation](https://continue.dev/docs)
- [Anthropic API Documentation](https://docs.anthropic.com/)

## Support

If you continue to have issues accessing AI assistants:
1. Check the plugin's documentation
2. Verify your IDE version compatibility
3. Contact the plugin support team
4. Check for known issues in the plugin's issue tracker
