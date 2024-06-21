## PR2

## Author
- #name
- #email

## Introducción

## Desarrollo

**Modelos**

Lo primero que he hecho en este proyecto es crear los nuevos modelos en la carpeta **src/main/java/uoc/ds/pr/model**.
Los nuevos modelos que he implementado son:
+ Port
+ Category
+ Product
+ Order

Cada modelo tiene sus atributos, constructor, getters y setters.

Además, he añadido el atributo **level** (*ShippingLinePR2.LoyaltyLevel*) en la clase Client. Por defecto el nivel del usuario es Bronze.

También he añadido la clase **LoyaltyLevel** en la carpeta **src/test/java/uoc/ds/pr/util** para pasar el test LoyaltyHelperTest. Tiene un método estático que devuelve el nivel correspondiente según el número entero que se le pase.

**Excepciones**

He creado todas las excepciones que se necesitaban para ShippingLinePR2Test y ShippingLinePR2 en la carpeta **src/main/java/uoc/ds/pr/exceptions**. Todas las excepciones extienden de DSException.

**ShippingLinePR2Impl**

He creado esta nueva clase para implementar los métodos necesarios para pasar los tests de ShipiningLinePR2Test. Esta clase extiende de ShipingLineImpl e implementa la interfaz ShippingLinePR2.

## Resultados tests

## Conclusión
