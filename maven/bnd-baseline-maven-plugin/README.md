# bnd-baseline-maven-plugin

The `bnd-baseline-maven-plugin` is a bnd based plugin that does semantic 
version checking of bundles.

## What does the `bnd-baseline-maven-plugin` do?

This plugin analyzes the packages exported by an OSGi bundle, and compares
them against the previous release of the bundle. Based on any observed API
changes the plugin will verify that the exported versions of the API 
packages have been updated correctly. 

By default the previous version of the bundle is found by querying the
configured artifact release repositories. Only released versions of the
bundle are considered as SNAPSHOT versions do not provide a stable base.


## How do I use the `bnd-baseline-maven-plugin` in my project?

Including the bnd-baseline-maven-plugin in your module is very easy:

    <plugin>
        <groupId>biz.aQute.bnd</groupId>
        <artifactId>bnd-baseline-maven-plugin</artifactId>
        <version>${bnd.version}</version>
        <configuration>...</configuration>
        <executions>
            <execution>
                <id>baseline</id>
                <goals>
                    <goal>baseline</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
    
### Running the `bnd-baseline-maven-plugin`

The only goal of the `bnd-baseline-maven-plugin` is `baseline` which verifies
the exported versions of the output bundle and its packages. By default the 
`bnd-baseline-maven-plugin` binds to the verify phase of your build.


### Configuring the `bnd-baseline-maven-plugin`

By default the `bnd-baseline-maven-plugin` does not need any configuration,
however there are several configuration options:

#### Controlling the Base artifact version

By default the `bnd-baseline-maven-plugin` will search for a base
artifact to compare against. The base artifact search will use the 
same groupId and artifactId as the executing project, and the highest
available version less than the current version of the project.

This behaviour does not normally need to be customised, however the
base search parameters can be overridden. If a base version is 
supplied then this is treated as a specific version to baseline against.
Any omitted values will be filled in with the normal defaults.


    <configuration>
        <base>
            <groupId>a.different.groupId</groupId>
            <artifactId>a.different.artifactId</artifactId>
            <version>1.2.3</version>
            <classifier>custom</classifier>
            <extension>war</extension>
        </base>
    </configuration>

#### Fail on missing base version

By default the `bnd-baseline-maven-plugin` will not fail the build if 
no base version can be determined. This is the default behaviour to
allow projects that have never been released before to enable baselining
from day one. If you do not want to allow this to happen
(i.e. you want to force a failure if no baselining occurs) then
set the `failOnMissing` property. 

    <configuration>
        <failOnMissing>true</failOnMissing>
    </configuration>

N.B. If a base version is determined but cannot be downloaded from the 
repository then the baselining operation will always fail.

#### Excluding Distribution Management

By default the `bnd-baseline-maven-plugin` includes the release repository
listed in distributionManagement, as well as configured artifact repositories.
This can be disabled using the `includeDistributionManagement` property. 

    <configuration>
        <includeDistributionManagement>true</includeDistributionManagement>
    </configuration>
