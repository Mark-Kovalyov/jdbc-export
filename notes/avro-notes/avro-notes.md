# Avro notes

## How to display the contents:

```
pip install fastavro

fastavro --schema out1.avro 
{
    "type": "record",
    "name": "defaultRecordName",
    "namespace": "defaultNamespace",
    "fields": [
        {
            "name": "id",
            "type": [
                "null",
                "long"
            ],
            "default": null
        },
        {
            "name": "category",
            "type": [
                "null",
                "int"
            ],
            "default": null
        },
        {
            "name": "name",
            "type": [
                "null",
                "string"
            ],
            "default": null
        },
        {
            "name": "size",
            "type": [
                "null",
                "double"
            ],
            "default": null
        },
        {
            "name": "last_update_time",
            "type": [
                "null",
                "string"
            ],
            "default": null
        }
    ]
}

```