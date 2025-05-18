#!/bin/bash

generate_p2updatesite_category(){
    header=$(<gama.experimental.p2updatesite/category_header.xml)
    user_cate=$(<gama.experimental.p2updatesite/category_body_user.xml)
    current_cate=$(<gama.experimental.p2updatesite/category_body.xml)
    footer=$(<gama.experimental.p2updatesite/category_footer.xml)

    cate=$'\n'$" "$'\n'
    for file in *"feature"*; do
        isInFile=$( cat gama.experimental.p2updatesite/category_body_user.xml | grep -c ${file})

        if [[ -f "$file/pom.xml" && ${file} != *"gama.experimental.parent"* &&  $isInFile -eq 0  ]]; then

            version=$(sed '/<parent>/,/<\/parent>/d;/<version>/!d;s/ *<\/\?version> *//g' "$file/pom.xml")
            if [ -z $version ]; then
                version=$(sed '/<version>/!d;s/ *<\/\?version> *//g' "$file/pom.xml" | sed 's/^[[:space:]]*//')
            fi

            q=$".qualifier"
            version=${version/-SNAPSHOT/$q}
            temp="<feature  url=\"features/"$file"_$version.jar\" id=\"$file\" version=\"$version\"> <category name=\"gama.optional\"/>   </feature>"
            temp=$(echo $temp|tr -d '\r' |tr -d '\n')
            cate="$cate $temp "$'\r'$'\n'

            #echo $temp
        else
            echo "Skipping $file"
            if [[ ! -f "$file/pom.xml" ]]; then
                echo "  No pom.xml properly set"
            fi
            if [[ ! $isInFile -eq 0 ]]; then
                echo "  Already set in gama.experimental.p2updatesite/category_body_user.xml"
            fi
            echo "===="
        fi;
    done

    if [[ "$current_cate" != "$cate" ]]; then
        echo "$cate" > gama.experimental.p2updatesite/category_body.xml
        echo "$header $cate $user_cate $footer " > gama.experimental.p2updatesite/category.xml
    fi
}

# ==============================================
#                   MAIN
# ==============================================


# Maven files
# ==============================================

rootPath=$( dirname "${BASH_SOURCE[0]}" )

#cd $rootPath

# Dynamicly generate pom.xml
cat $rootPath/gama.experimental.parent/pom_header.xml > $rootPath/gama.experimental.parent/pom.xml && \
find . -maxdepth 2 -type f \
    -name "pom.xml" \
    -not -wholename "*parent*" \
    -not -wholename "*p2updatesite*" \
    -exec sh -c 'echo "<module>../$(echo {} | cut -d "/" -f 2)</module>"' \; >> $rootPath/gama.experimental.parent/pom.xml && \
echo "<module>../gama.experimental.p2updatesite</module>" >> $rootPath/gama.experimental.parent/pom.xml && \ # Enforce update site is ran as latest
cat $rootPath/gama.experimental.parent/pom_footer.xml >> $rootPath/gama.experimental.parent/pom.xml
# generate_parent_pom

# Maven to update p2 site
# ==============================================
generate_p2updatesite_category

# Compiling and publishing experimentals
# ==============================================
#
cd $rootPath/gama.experimental.parent

if [[ $GPG_PASSPHRASE != "" ]]; then
    mvn verify -Dgpg.passphrase=$GPG_PASSPHRASE --settings ../settings.xml
fi

mvn clean install -B -Dtycho.p2.transport.min-cache-minutes=0 -Dtycho.equinox.resolver.uses=true -P p2Repo --settings ../settings.xml && \
cd -
