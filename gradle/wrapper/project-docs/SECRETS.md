# Secure Secrets Management

Since we cannot commit `secrets.properties` to the repository (security risk), we use an **encrypted version** committed to Git.

## 1. Prerequisites

### For Mac/Linux
You already have `openssl` installed.

### For Windows
You must use **Git Bash** (included with Git for Windows). Do not use Command Prompt or PowerShell.

---

## 2. How to Decrypt (For Team Members)

When you pull the repo, you will see `secrets.enc`. To get the actual `secrets.properties` file:

1.  Ask a team admin for the **decryption password**.
2.  Run this command in your terminal (root of the project):

    ```bash 
    openssl enc -aes-256-cbc -pbkdf2 -d -in secrets.enc -out secrets.properties
    ```

3.  The `secrets.properties` file will appear. **DO NOT commit this file.**

---

## 3. How to Update & Re-Encrypt (For Admins)

If you add a new secret or change a password:

1.  Edit `secrets.properties`.
2.  Run this command to update the encrypted file:

    ```bash
    openssl enc -aes-256-cbc -pbkdf2 -salt -in secrets.properties -out secrets.enc
    ```

3.  Commit `secrets.enc` to Git:

    ```bash
    git add secrets.enc
    git commit -m "chore: update encrypted secrets"
    git push
    ```
