// models/user.model.js

class User {
  constructor(id, name, email, phone, password, image) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.password = password; // à hacher plus tard avec bcrypt
    this.image = image; // URL ou nom de fichier de l’image de profil
  }
}

module.exports = User;
