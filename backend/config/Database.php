<?php
/**
 * PDO connection to the MySQL/MariaDB database (XAMPP defaults).
 * Adjust the constants below if your local setup differs.
 */
require_once __DIR__ . '/../helpers/Response.php';

class Database
{
    private const HOST = 'localhost';
    private const NAME = 'cps';
    private const USER = 'root';
    private const PASS = '';

    public static function connect(): PDO
    {
        $dsn = 'mysql:host=' . self::HOST . ';dbname=' . self::NAME . ';charset=utf8mb4';
        try {
            return new PDO($dsn, self::USER, self::PASS, [
                PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES   => false,
            ]);
        } catch (PDOException $e) {
            Response::error('Database connection failed: ' . $e->getMessage(), 500);
        }
    }
}
