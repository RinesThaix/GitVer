GitVer
========

It is a maven plugin which allows to get git version right into build properties.

Example Configuration
---------------------
```xml
<plugin>
    <groupId>ru.luvas</groupId>
    <artifactId>gitver</artifactId>
    <version>1.0</version>
    <executions>
        <execution>
            <phase>initialize</phase>
            <goals>
                <goal>describe</goal>
            </goals>
        </execution>
    </executions>
</plugin>
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <version>2.5</version>
    <configuration>
      <archive>
        <manifestEntries>
          <Implementation-Version>${describe}</Implementation-Version>
        </manifestEntries>
      </archive>
    </configuration>
</plugin>
```